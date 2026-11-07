package com.marithe.sysfact.dao;

import com.marithe.sysfact.model.Venta;
import com.marithe.sysfact.model.DetalleVenta;
import java.util.List;

public interface VentaDao {

    List<Venta> getList(Venta obj);

    Venta getById(Long id);

    Venta insert(Venta venta);

    void insertDetalle(DetalleVenta detalle, Long ventaId);

    void descontarStock(Long articuloId, Long cantidad);

    List<DetalleVenta> getDetallesByVentaId(Long ventaId);
}