package com.marithe.sysfact.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.marithe.sysfact.service.VentaService;
import com.marithe.sysfact.model.Venta;
import com.marithe.sysfact.model.DetalleVenta;
import com.marithe.sysfact.dao.VentaDao;
import java.util.List;

@Service
public class VentaServiceImpl implements VentaService {

    @Autowired
    private VentaDao ventaDao;

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
        Venta nuevaVenta = ventaDao.insert(venta);

        if (venta.getDetallesVenta() != null) {
            for (DetalleVenta detalle : venta.getDetallesVenta()) {
                ventaDao.insertDetalle(detalle, nuevaVenta.getId());
                ventaDao.descontarStock(detalle.getArticulo().getId(), detalle.getCantidad());
            }
        }
        return nuevaVenta;
    }
}