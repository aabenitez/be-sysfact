package com.marithe.sysfact.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.marithe.sysfact.service.AuditoriaService;
import com.marithe.sysfact.service.StockService;
import com.marithe.sysfact.service.VentaService;
import com.marithe.sysfact.exception.BusinessLogicException;
import com.marithe.sysfact.model.Articulo;
import com.marithe.sysfact.model.Auditoria;
import com.marithe.sysfact.model.Cliente;
import com.marithe.sysfact.model.Venta;
import com.marithe.sysfact.model.DetalleVenta;
import com.marithe.sysfact.dao.ArticuloDao;
import com.marithe.sysfact.dao.CobroDao;
import com.marithe.sysfact.service.CobroService;
import com.marithe.sysfact.dao.VentaDao;
import com.marithe.sysfact.security.UsuarioActual;
import com.marithe.sysfact.util.CalculadoraIva;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class VentaServiceImpl implements VentaService {

    static final int MOTIVO_MIN = 5;
    static final int MOTIVO_MAX = 250;

    @Autowired
    private VentaDao ventaDao;

    @Autowired
    private ArticuloDao articuloDao;

    @Autowired
    private AuditoriaService auditoria;

    @Autowired
    private UsuarioActual usuarioActual;

    @Autowired
    private StockService stockService;

    @Autowired
    private CobroDao cobroDao;

    @Autowired
    private CobroService cobroService;

    @Override
    public List<Venta> getList(Venta obj) {
        List<Venta> ventas = ventaDao.getList(obj);
        int plazo = cobroService.plazoDias();
        // Para cada venta de la lista, buscamos y asociamos sus detalles
        for (Venta venta : ventas) {
            venta.setDetallesVenta(ventaDao.getDetallesByVentaId(venta.getId()));
            ponerVencimiento(venta, plazo);
        }
        return ventas;
    }

    @Override
    public Venta getById(Long id) {
        Venta venta = ventaDao.getById(id);
        if (venta != null) {
            venta.setDetallesVenta(ventaDao.getDetallesByVentaId(venta.getId()));
            ponerVencimiento(venta, cobroService.plazoDias());
        }
        return venta;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Venta insert(Venta venta) {
        // The seller is whoever is logged in, never what the request says.
        venta.setUsuario(usuarioActual.get());
        prepararLineas(venta, null);

        Venta nuevaVenta = ventaDao.insert(venta);
        insertarLineas(venta, nuevaVenta.getId());
        stockService.moverPorVenta(nuevaVenta.getId(), diferencia(venta.getDetallesVenta(), null),
                "Factura N.º " + numero(nuevaVenta.getId()));

        Venta guardada = getById(nuevaVenta.getId());
        auditoria.registrar(Auditoria.CREAR, Auditoria.VENTA, guardada.getId(), resumen(guardada), null, guardada);
        return guardada;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Venta update(Long id, Venta venta) {
        Venta actual = getById(id);
        if (actual == null) {
            return null;
        }
        validarModificable(actual, "editar");

        venta.setId(id);
        prepararLineas(venta, actual);
        validarContraCobros(actual, venta);

        if (!ventaDao.update(venta)) {
            throw new BusinessLogicException("La factura N.º " + numero(id) + " ya no se puede editar: fue anulada.");
        }
        ventaDao.deleteDetalles(id);
        insertarLineas(venta, id);
        // Only the net change per article moves stock (2 → 3 macetas is one more out, not 2 in and 3 out).
        stockService.moverPorVenta(id, diferencia(venta.getDetallesVenta(), actual.getDetallesVenta()),
                "Edición de la factura N.º " + numero(id));

        Venta despues = getById(id);
        auditoria.registrar(Auditoria.EDITAR, Auditoria.VENTA, id,
                "Factura N.º " + numero(id) + ": " + cambios(actual, despues), actual, despues);
        return despues;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Venta anular(Long id, String motivo) {
        String m = motivo == null ? "" : motivo.trim();
        if (m.length() < MOTIVO_MIN) {
            throw new BusinessLogicException("Escribí el motivo de la anulación (al menos " + MOTIVO_MIN + " caracteres).");
        }
        if (m.length() > MOTIVO_MAX) {
            throw new BusinessLogicException("El motivo puede tener hasta " + MOTIVO_MAX + " caracteres.");
        }
        Venta actual = getById(id);
        if (actual == null) {
            return null;
        }
        validarModificable(actual, "anular");
        BigDecimal cobrado = cobrado(id);
        if (cobrado.signum() > 0) {
            throw new BusinessLogicException("La factura N.º " + numero(id) + " tiene cobros por " + gs(cobrado)
                    + ". Anulá primero los cobros.");
        }

        if (!ventaDao.anular(id, usuarioActual.get().getId(), m)) {
            throw new BusinessLogicException("La factura N.º " + numero(id) + " ya está anulada.");
        }
        stockService.moverPorVenta(id, diferencia(null, actual.getDetallesVenta()),
                "Anulación de la factura N.º " + numero(id) + ": " + m);

        Venta despues = getById(id);
        auditoria.registrar(Auditoria.ANULAR, Auditoria.VENTA, id,
                "Factura N.º " + numero(id) + " (" + gs(actual.getTotalFactura()) + "). Motivo: " + m, actual, despues);
        return despues;
    }

    @Override
    public List<Auditoria> historial(Long id) {
        return auditoria.historial(Auditoria.VENTA, id);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Cancelled sales cannot change. A non-administrator may only change sales of the same day:
     * older ones may already be in the cash count or in the books.
     */
    private void validarModificable(Venta actual, String accion) {
        if (Venta.ANULADA.equals(actual.getEstado())) {
            throw new BusinessLogicException("La factura N.º " + numero(actual.getId()) + " está anulada y no se puede "
                    + accion + ".");
        }
        if (!usuarioActual.esAdministrador() && !esDeHoy(actual)) {
            throw new BusinessLogicException("Solo un Administrador puede " + accion
                    + " facturas de días anteriores. Esta es del " + fechaCorta(actual) + ".");
        }
    }

    /**
     * Once a credit sale has payments, an edit may not leave them pointing at something else: it
     * stays on credit, with the same client, and its total cannot go below what was already paid.
     * Takes the sale's lock, so a payment cannot slip in between this check and the update.
     */
    private void validarContraCobros(Venta actual, Venta nueva) {
        BigDecimal cobrado = cobrado(actual.getId());
        if (cobrado.signum() <= 0) {
            return;
        }
        String f = "La factura N.º " + numero(actual.getId()) + " tiene cobros por " + gs(cobrado);
        if (!"CRE".equals(nueva.getCondicionVenta())) {
            throw new BusinessLogicException(f + ": tiene que seguir siendo a crédito.");
        }
        Long antes = actual.getCliente() == null ? null : actual.getCliente().getId();
        Long despues = nueva.getCliente() == null ? null : nueva.getCliente().getId();
        if (!Objects.equals(antes, despues)) {
            throw new BusinessLogicException(f + ": no se puede cambiar el cliente.");
        }
        if (nueva.getTotalFactura() != null && nueva.getTotalFactura().compareTo(cobrado) < 0) {
            throw new BusinessLogicException(f + ": el total no puede quedar por debajo de eso (quedaría "
                    + gs(nueva.getTotalFactura()) + ").");
        }
    }

    /** Valid payments of the sale, locking its row for the rest of the transaction. */
    private BigDecimal cobrado(Long ventaId) {
        BigDecimal c = cobroDao.cobradoParaActualizar(ventaId);
        return c == null ? BigDecimal.ZERO : c;
    }

    private static void ponerVencimiento(Venta v, int plazoDias) {
        if ("CRE".equals(v.getCondicionVenta()) && v.getFecha() != null) {
            LocalDate vence = fecha(v).plusDays(plazoDias);
            v.setVencimiento(vence.toString());
        }
    }

    /** Validates the lines, checks prices and recomputes IVA and totals. */
    private void prepararLineas(Venta venta, Venta anterior) {
        if (!"CON".equals(venta.getCondicionVenta()) && !"CRE".equals(venta.getCondicionVenta())) {
            throw new BusinessLogicException("La condición de venta tiene que ser contado o crédito.");
        }
        if (venta.getCliente() != null && venta.getCliente().getId() == null) {
            venta.setCliente(null);
        }
        if ("CRE".equals(venta.getCondicionVenta()) && venta.getCliente() == null) {
            throw new BusinessLogicException("Para vender a crédito hay que elegir un cliente.");
        }
        Map<Long, Articulo> articulos = new HashMap<>();
        if (venta.getDetallesVenta() != null) {
            for (DetalleVenta d : venta.getDetallesVenta()) {
                Articulo a = articulo(d, articulos);
                if (d.getPrecioVenta() == null) {
                    d.setPrecioVenta(a.getPrecio());
                }
                validarPrecio(d, a, anterior);
            }
        }
        // El IVA y los totales se calculan aquí con la tasa de cada artículo;
        // no se confía en los montos que envía el cliente.
        CalculadoraIva.calcular(venta, d -> articulos.get(d.getArticulo().getId()).getTasaIva());
    }

    /**
     * Only an administrator may sell below or above the list price. Others may keep the price a line
     * already had in the sale being edited, so a later price change does not block the edit.
     */
    private void validarPrecio(DetalleVenta d, Articulo a, Venta anterior) {
        if (usuarioActual.esAdministrador() || mismoMonto(d.getPrecioVenta(), a.getPrecio())) {
            return;
        }
        if (anterior != null) {
            for (DetalleVenta previo : anterior.getDetallesVenta()) {
                if (Objects.equals(previo.getArticulo().getId(), a.getId())
                        && mismoMonto(previo.getPrecioVenta(), d.getPrecioVenta())) {
                    return;
                }
            }
        }
        throw new BusinessLogicException("Solo un Administrador puede cambiar el precio de \"" + a.getNombre().trim()
                + "\" (precio de lista " + gs(a.getPrecio()) + ").");
    }

    private void insertarLineas(Venta venta, Long ventaId) {
        for (DetalleVenta detalle : venta.getDetallesVenta()) {
            ventaDao.insertDetalle(detalle, ventaId);
        }
    }

    /**
     * Stock change per article when a sale goes from the lines in {@code antes} to those in
     * {@code ahora} (either may be null): units sold come out (negative), units no longer sold go back.
     */
    static Map<Long, Long> diferencia(List<DetalleVenta> ahora, List<DetalleVenta> antes) {
        Map<Long, Long> cambios = new LinkedHashMap<>();
        if (ahora != null) {
            for (DetalleVenta d : ahora) {
                cambios.merge(d.getArticulo().getId(), -cantidad(d), Long::sum);
            }
        }
        if (antes != null) {
            for (DetalleVenta d : antes) {
                cambios.merge(d.getArticulo().getId(), cantidad(d), Long::sum);
            }
        }
        return cambios;
    }

    private static long cantidad(DetalleVenta d) {
        return d.getCantidad() == null ? 0 : d.getCantidad();
    }

    private Articulo articulo(DetalleVenta detalle, Map<Long, Articulo> cache) {
        if (detalle.getArticulo() == null || detalle.getArticulo().getId() == null) {
            throw new BusinessLogicException("Cada línea de la venta debe indicar el artículo.");
        }
        return cache.computeIfAbsent(detalle.getArticulo().getId(), key -> {
            Articulo articulo = articuloDao.getById(key);
            if (articulo == null) {
                throw new BusinessLogicException("El artículo " + key + " no existe.");
            }
            return articulo;
        });
    }

    // --- Texts for the audit log -----------------------------------------------------------------

    static String resumen(Venta v) {
        int unidades = 0;
        for (DetalleVenta d : v.getDetallesVenta()) {
            unidades += d.getCantidad();
        }
        return "Factura N.º " + numero(v.getId()) + " · " + condicion(v.getCondicionVenta()) + " · "
                + gs(v.getTotalFactura()) + " · " + unidades + (unidades == 1 ? " unidad" : " unidades")
                + " · " + cliente(v.getCliente());
    }

    /** Human-readable list of what changed between two versions of a sale. */
    static String cambios(Venta antes, Venta despues) {
        List<String> cambios = new ArrayList<>();
        if (!Objects.equals(antes.getCondicionVenta(), despues.getCondicionVenta())) {
            cambios.add("condición " + condicion(antes.getCondicionVenta()) + " → " + condicion(despues.getCondicionVenta()));
        }
        Long clienteAntes = antes.getCliente() == null ? null : antes.getCliente().getId();
        Long clienteDespues = despues.getCliente() == null ? null : despues.getCliente().getId();
        if (!Objects.equals(clienteAntes, clienteDespues)) {
            cambios.add("cliente " + cliente(antes.getCliente()) + " → " + cliente(despues.getCliente()));
        }

        Map<Long, String> nombres = new HashMap<>();
        Map<Long, Long> cantAntes = cantidades(antes, nombres);
        Map<Long, Long> cantDespues = cantidades(despues, nombres);
        for (Map.Entry<Long, Long> e : cantDespues.entrySet()) {
            Long previa = cantAntes.get(e.getKey());
            if (previa == null) {
                cambios.add("agregó " + e.getValue() + " × " + nombres.get(e.getKey()));
            } else if (!previa.equals(e.getValue())) {
                cambios.add(nombres.get(e.getKey()) + " " + previa + " → " + e.getValue());
            }
        }
        for (Map.Entry<Long, Long> e : cantAntes.entrySet()) {
            if (!cantDespues.containsKey(e.getKey())) {
                cambios.add("quitó " + e.getValue() + " × " + nombres.get(e.getKey()));
            }
        }
        if (!mismoMonto(antes.getTotalDescuento(), despues.getTotalDescuento())) {
            cambios.add("descuentos " + gs(antes.getTotalDescuento()) + " → " + gs(despues.getTotalDescuento()));
        }
        if (!mismoMonto(antes.getTotalFactura(), despues.getTotalFactura())) {
            cambios.add("total " + gs(antes.getTotalFactura()) + " → " + gs(despues.getTotalFactura()));
        }
        return cambios.isEmpty() ? "sin cambios en los datos" : String.join("; ", cambios);
    }

    private static Map<Long, Long> cantidades(Venta v, Map<Long, String> nombres) {
        Map<Long, Long> m = new LinkedHashMap<>();
        for (DetalleVenta d : v.getDetallesVenta()) {
            Long id = d.getArticulo().getId();
            m.merge(id, d.getCantidad(), Long::sum);
            String nombre = d.getArticulo().getNombre();
            nombres.putIfAbsent(id, nombre == null ? "artículo " + id : nombre.trim());
        }
        return m;
    }

    private boolean esDeHoy(Venta v) {
        return v.getFecha() != null && fecha(v).equals(LocalDate.now());
    }

    private static LocalDate fecha(Venta v) {
        return v.getFecha().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private static String fechaCorta(Venta v) {
        LocalDate f = fecha(v);
        return String.format("%02d/%02d/%d", f.getDayOfMonth(), f.getMonthValue(), f.getYear());
    }

    static String numero(Long id) {
        return String.format("%06d", id);
    }

    private static String condicion(String c) {
        return "CON".equals(c) ? "Contado" : "CRE".equals(c) ? "Crédito" : String.valueOf(c);
    }

    private static String cliente(Cliente c) {
        if (c == null) {
            return "sin cliente";
        }
        String nombre = ((c.getNombres() == null ? "" : c.getNombres().trim()) + " "
                + (c.getApellidos() == null ? "" : c.getApellidos().trim())).trim();
        return nombre.isEmpty() ? "cliente " + c.getId() : nombre;
    }

    private static boolean mismoMonto(BigDecimal a, BigDecimal b) {
        BigDecimal x = a == null ? BigDecimal.ZERO : a;
        BigDecimal y = b == null ? BigDecimal.ZERO : b;
        return x.compareTo(y) == 0;
    }

    static String gs(BigDecimal monto) {
        DecimalFormatSymbols simbolos = new DecimalFormatSymbols();
        simbolos.setGroupingSeparator('.');
        DecimalFormat f = new DecimalFormat("#,##0", simbolos);
        return "Gs. " + f.format((monto == null ? BigDecimal.ZERO : monto).setScale(0, RoundingMode.HALF_UP));
    }
}
