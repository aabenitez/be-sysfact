package com.marithe.sysfact.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.marithe.sysfact.dao.ArticuloDao;
import com.marithe.sysfact.dao.CobroDao;
import com.marithe.sysfact.model.Cliente;
import com.marithe.sysfact.service.CobroService;
import com.marithe.sysfact.dao.VentaDao;
import com.marithe.sysfact.exception.BusinessLogicException;
import com.marithe.sysfact.model.Articulo;
import com.marithe.sysfact.model.Auditoria;
import com.marithe.sysfact.model.DetalleVenta;
import com.marithe.sysfact.model.Usuario;
import com.marithe.sysfact.model.Venta;
import com.marithe.sysfact.security.UsuarioActual;
import com.marithe.sysfact.service.AuditoriaService;
import com.marithe.sysfact.service.StockService;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class VentaServiceImplTest {

	private static final long MACETA = 10L; // Gs. 20.000, IVA 10 %
	private static final long TIERRA = 11L; // Gs. 5.000, IVA 10 %

	@Mock
	private VentaDao ventaDao;
	@Mock
	private ArticuloDao articuloDao;
	@Mock
	private AuditoriaService auditoria;
	@Mock
	private UsuarioActual usuarioActual;
	@Mock
	private StockService stockService;
	@Mock
	private CobroDao cobroDao;
	@Mock
	private CobroService cobroService;
	@InjectMocks
	private VentaServiceImpl service;

	private final Usuario cajero = new Usuario();

	@BeforeEach
	void setUp() {
		cajero.setId(3L);
		cajero.setNombres("Caja");
		cajero.setApellidos("Uno");
		when(usuarioActual.get()).thenReturn(cajero);
		when(usuarioActual.esAdministrador()).thenReturn(false);
		when(articuloDao.getById(MACETA)).thenReturn(articulo(MACETA, "Maceta", 20000));
		when(articuloDao.getById(TIERRA)).thenReturn(articulo(TIERRA, "Tierra", 5000));
		when(ventaDao.update(any())).thenReturn(true);
		when(ventaDao.anular(anyLong(), anyLong(), anyString())).thenReturn(true);
		when(cobroDao.cobradoParaActualizar(anyLong())).thenReturn(BigDecimal.ZERO);
		when(cobroService.plazoDias()).thenReturn(30);
	}

	private static Articulo articulo(long id, String nombre, long precio) {
		Articulo a = new Articulo();
		a.setId(id);
		a.setNombre(nombre);
		a.setPrecio(BigDecimal.valueOf(precio));
		a.setTasaIva(10);
		return a;
	}

	private static DetalleVenta linea(long articuloId, long cantidad, Long precio) {
		DetalleVenta d = new DetalleVenta();
		Articulo a = new Articulo();
		a.setId(articuloId);
		a.setNombre(articuloId == MACETA ? "Maceta" : "Tierra");
		d.setArticulo(a);
		d.setCantidad(cantidad);
		d.setPrecioVenta(precio == null ? null : BigDecimal.valueOf(precio));
		d.setDescuento(BigDecimal.ZERO);
		return d;
	}

	private static Venta venta(Long id, LocalDate dia, String estado, DetalleVenta... lineas) {
		Venta v = new Venta();
		v.setId(id);
		v.setCondicionVenta("CON");
		v.setEstado(estado);
		v.setFecha(dia == null ? null : Date.from(dia.atTime(10, 0).atZone(ZoneId.systemDefault()).toInstant()));
		v.setDetallesVenta(new ArrayList<>(List.of(lineas)));
		return v;
	}

	/** Makes the DAO return this sale (header and lines) for its id. */
	private void existe(Venta v) {
		when(ventaDao.getById(v.getId())).thenReturn(v);
		when(ventaDao.getDetallesByVentaId(v.getId())).thenReturn(v.getDetallesVenta());
	}

	@Test
	void insertUsesTheLoggedInSellerAndAudits() {
		Venta nueva = venta(null, null, null, linea(MACETA, 2, null));
		Usuario otro = new Usuario();
		otro.setId(99L);
		nueva.setUsuario(otro);
		when(ventaDao.insert(any())).thenAnswer(i -> {
			Venta v = i.getArgument(0);
			v.setId(50L);
			existe(v);
			return v;
		});

		Venta creada = service.insert(nueva);

		assertEquals(3L, creada.getUsuario().getId());
		assertEquals(new BigDecimal("40000"), creada.getTotalFactura());
		verify(stockService).moverPorVenta(50L, Map.of(MACETA, -2L), "Factura N.º 000050");
		verify(auditoria).registrar(eq(Auditoria.CREAR), eq(Auditoria.VENTA), eq(50L), anyString(), isNull(), any());
	}

	@Test
	void cashierCannotChangeTheListPrice() {
		Venta nueva = venta(null, null, null, linea(MACETA, 1, 15000L));
		BusinessLogicException e = assertThrows(BusinessLogicException.class, () -> service.insert(nueva));
		assertTrue(e.getMessage().contains("Solo un Administrador puede cambiar el precio"));
		verify(ventaDao, never()).insert(any());
	}

	@Test
	void administratorCanChangeThePrice() {
		when(usuarioActual.esAdministrador()).thenReturn(true);
		when(ventaDao.insert(any())).thenAnswer(i -> {
			Venta v = i.getArgument(0);
			v.setId(51L);
			existe(v);
			return v;
		});
		Venta creada = service.insert(venta(null, null, null, linea(MACETA, 1, 15000L)));
		assertEquals(new BigDecimal("15000"), creada.getTotalFactura());
	}

	@Test
	void cashierCannotEditASaleFromAnotherDay() {
		existe(venta(7L, LocalDate.now().minusDays(1), Venta.EMITIDA, linea(MACETA, 1, 20000L)));
		BusinessLogicException e = assertThrows(BusinessLogicException.class,
				() -> service.update(7L, venta(null, null, null, linea(MACETA, 2, null))));
		assertTrue(e.getMessage().contains("días anteriores"));
		verify(ventaDao, never()).update(any());
	}

	@Test
	void cancelledSaleCannotBeEdited() {
		when(usuarioActual.esAdministrador()).thenReturn(true);
		existe(venta(7L, LocalDate.now(), Venta.ANULADA, linea(MACETA, 1, 20000L)));
		BusinessLogicException e = assertThrows(BusinessLogicException.class,
				() -> service.update(7L, venta(null, null, null, linea(MACETA, 2, null))));
		assertTrue(e.getMessage().contains("anulada"));
	}

	@Test
	void editReturnsOldStockTakesNewStockAndAuditsTheChanges() {
		Venta original = venta(8L, LocalDate.now(), Venta.EMITIDA, linea(MACETA, 1, 20000L));
		original.setTotalFactura(new BigDecimal("20000"));
		existe(original);
		Venta editada = venta(8L, LocalDate.now(), Venta.EMITIDA, linea(MACETA, 3, 20000L), linea(TIERRA, 1, 5000L));
		editada.setTotalFactura(new BigDecimal("65000"));
		// After the update the DAO answers with the new version.
		when(ventaDao.update(any())).thenAnswer(i -> {
			existe(editada);
			return true;
		});

		service.update(8L, venta(null, null, null, linea(MACETA, 3, null), linea(TIERRA, 1, null)));

		verify(ventaDao).deleteDetalles(8L);
		// Net change only: 1 → 3 macetas is 2 more out; the tierra is new.
		verify(stockService).moverPorVenta(8L, Map.of(MACETA, -2L, TIERRA, -1L), "Edición de la factura N.º 000008");
		ArgumentCaptor<String> detalle = ArgumentCaptor.forClass(String.class);
		verify(auditoria).registrar(eq(Auditoria.EDITAR), eq(Auditoria.VENTA), eq(8L), detalle.capture(), any(), any());
		assertTrue(detalle.getValue().contains("Maceta 1 → 3"), detalle.getValue());
		assertTrue(detalle.getValue().contains("agregó 1 × Tierra"), detalle.getValue());
		assertTrue(detalle.getValue().contains("total Gs. 20.000 → Gs. 65.000"), detalle.getValue());
	}

	@Test
	void cancelRequiresAReason() {
		existe(venta(9L, LocalDate.now(), Venta.EMITIDA, linea(MACETA, 1, 20000L)));
		assertThrows(BusinessLogicException.class, () -> service.anular(9L, "  "));
		assertThrows(BusinessLogicException.class, () -> service.anular(9L, "abc"));
		verify(ventaDao, never()).anular(anyLong(), anyLong(), anyString());
	}

	@Test
	void cancelReturnsStockAndAuditsTheReason() {
		Venta v = venta(9L, LocalDate.now(), Venta.EMITIDA, linea(MACETA, 2, 20000L));
		v.setTotalFactura(new BigDecimal("40000"));
		existe(v);

		service.anular(9L, "  Cliente devolvió la compra ");

		verify(ventaDao).anular(9L, 3L, "Cliente devolvió la compra");
		verify(stockService).moverPorVenta(9L, Map.of(MACETA, 2L),
				"Anulación de la factura N.º 000009: Cliente devolvió la compra");
		ArgumentCaptor<String> detalle = ArgumentCaptor.forClass(String.class);
		verify(auditoria).registrar(eq(Auditoria.ANULAR), eq(Auditoria.VENTA), eq(9L), detalle.capture(), any(), any());
		assertEquals("Factura N.º 000009 (Gs. 40.000). Motivo: Cliente devolvió la compra", detalle.getValue());
	}

	@Test
	void cancelOfMissingSaleReturnsNull() {
		assertNull(service.anular(404L, "No existe la venta"));
	}

	// --- Credit sales with payments ---------------------------------------------------------------

	private static Cliente cliente(long id) {
		Cliente c = new Cliente();
		c.setId(id);
		c.setNombres("Ana");
		c.setApellidos("Pérez");
		return c;
	}

	/** A credit sale of today for client 1, 2 macetas (Gs. 40.000), with Gs. 15.000 already paid. */
	private Venta creditoConCobros() {
		Venta v = venta(12L, LocalDate.now(), Venta.EMITIDA, linea(MACETA, 2, 20000L));
		v.setCondicionVenta("CRE");
		v.setCliente(cliente(1));
		v.setTotalFactura(new BigDecimal("40000"));
		existe(v);
		when(cobroDao.cobradoParaActualizar(12L)).thenReturn(new BigDecimal("15000"));
		return v;
	}

	private static Venta credito(long clienteId, DetalleVenta... lineas) {
		Venta v = venta(null, null, null, lineas);
		v.setCondicionVenta("CRE");
		v.setCliente(cliente(clienteId));
		return v;
	}

	@Test
	void saleWithPaymentsCannotBeCancelled() {
		creditoConCobros();
		BusinessLogicException e = assertThrows(BusinessLogicException.class,
				() -> service.anular(12L, "El cliente devolvió todo"));
		assertTrue(e.getMessage().contains("tiene cobros por Gs. 15.000"), e.getMessage());
		verify(ventaDao, never()).anular(anyLong(), anyLong(), anyString());
	}

	@Test
	void saleWithPaymentsStaysOnCreditWithTheSameClient() {
		creditoConCobros();
		Venta contado = venta(null, null, null, linea(MACETA, 2, null));
		contado.setCliente(cliente(1));
		BusinessLogicException e1 = assertThrows(BusinessLogicException.class, () -> service.update(12L, contado));
		assertTrue(e1.getMessage().contains("seguir siendo a crédito"), e1.getMessage());

		BusinessLogicException e2 = assertThrows(BusinessLogicException.class,
				() -> service.update(12L, credito(2, linea(MACETA, 2, null))));
		assertTrue(e2.getMessage().contains("no se puede cambiar el cliente"), e2.getMessage());
		verify(ventaDao, never()).update(any());
	}

	@Test
	void saleWithPaymentsCannotGoBelowWhatWasPaid() {
		creditoConCobros();
		// One tierra: Gs. 5.000 < Gs. 15.000 already paid.
		BusinessLogicException e = assertThrows(BusinessLogicException.class,
				() -> service.update(12L, credito(1, linea(TIERRA, 1, null))));
		assertTrue(e.getMessage().contains("no puede quedar por debajo"), e.getMessage());
		verify(ventaDao, never()).update(any());
	}

	@Test
	void saleWithPaymentsCanStillBeEditedAboveWhatWasPaid() {
		creditoConCobros();
		service.update(12L, credito(1, linea(MACETA, 1, null)));
		verify(ventaDao).update(any());
	}

	@Test
	void creditSaleGetsItsDueDateAndBalance() {
		Venta v = creditoConCobros();
		v.setCobrado(new BigDecimal("15000"));
		Venta leida = service.getById(12L);
		assertEquals(LocalDate.now().plusDays(30).toString(), leida.getVencimiento());
		assertEquals(new BigDecimal("25000"), leida.getSaldo());
	}
}
