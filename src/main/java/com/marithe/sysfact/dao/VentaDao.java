package com.marithe.sysfact.dao;

import com.marithe.sysfact.model.Venta;
import com.marithe.sysfact.model.DetalleVenta;
import java.util.List;

public interface VentaDao {

    List<Venta> getList(Venta obj);

    Venta getById(Long id);

    Venta insert(Venta venta);

    void insertDetalle(DetalleVenta detalle, Long ventaId);

    List<DetalleVenta> getDetallesByVentaId(Long ventaId);

    /** Updates the header of an issued sale; returns false if it does not exist or is cancelled. */
    boolean update(Venta venta);

    void deleteDetalles(Long ventaId);

    /** Marks an issued sale as cancelled; returns false if it does not exist or was already cancelled. */
    boolean anular(Long ventaId, Long usuarioId, String motivo);
}