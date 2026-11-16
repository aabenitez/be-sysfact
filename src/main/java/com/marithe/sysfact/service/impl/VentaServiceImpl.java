package com.marithe.sysfact.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.marithe.sysfact.service.VentaService;
import com.marithe.sysfact.exception.BusinessLogicException;
import com.marithe.sysfact.model.Articulo;
import com.marithe.sysfact.model.Venta;
import com.marithe.sysfact.model.DetalleVenta;
import com.marithe.sysfact.dao.ArticuloDao;
import com.marithe.sysfact.dao.VentaDao;
import com.marithe.sysfact.util.CalculadoraIva;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class VentaServiceImpl implements VentaService {

    @Autowired
    private VentaDao ventaDao;

    @Autowired
    private ArticuloDao articuloDao;

    @Override
    public List<Venta> getList(Venta obj) {
        List<Venta> ventas = ventaDao.getList(obj);
        // Para cada venta de la lista, buscamos y asociamos sus detalles
        for (Venta venta : ventas) {
            venta.setDetallesVenta(ventaDao.getDetallesByVentaId(venta.getId()));
        }
        return ventas;
    }

    @Override
    public Venta getById(Long id) {
        Venta venta = ventaDao.getById(id);
        if (venta != null) {
            venta.setDetallesVenta(ventaDao.getDetallesByVentaId(venta.getId()));
        }
        return venta;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Venta insert(Venta venta) {
        // El IVA y los totales se calculan aquí con la tasa de cada artículo;
        // no se confía en los montos que envía el cliente.
        Map<Long, Integer> tasas = new HashMap<>();
        CalculadoraIva.calcular(venta, detalle -> tasaDelArticulo(detalle, tasas));

        Venta nuevaVenta = ventaDao.insert(venta);
        for (DetalleVenta detalle : venta.getDetallesVenta()) {
            ventaDao.insertDetalle(detalle, nuevaVenta.getId());
            ventaDao.descontarStock(detalle.getArticulo().getId(), detalle.getCantidad());
        }
        return nuevaVenta;
    }

    private int tasaDelArticulo(DetalleVenta detalle, Map<Long, Integer> cache) {
        if (detalle.getArticulo() == null || detalle.getArticulo().getId() == null) {
            throw new BusinessLogicException("Cada línea de la venta debe indicar el artículo.");
        }
        Long id = detalle.getArticulo().getId();
        return cache.computeIfAbsent(id, key -> {
            Articulo articulo = articuloDao.getById(key);
            if (articulo == null) {
                throw new BusinessLogicException("El artículo " + key + " no existe.");
            }
            return articulo.getTasaIva();
        });
    }
}
