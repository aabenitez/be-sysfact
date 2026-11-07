package com.marithe.sysfact.service;

import com.marithe.sysfact.model.Venta;
import java.util.List;

public interface VentaService {

    List<Venta> getList(Venta obj);

    Venta getById(Long id);

    Venta insert(Venta obj);

    /* Habitualmente las facturas/ventas no se modifican ni se eliminan por integridad contable,
       pero si la regla de negocio lo requiere más adelante, se pueden añadir aquí. */
}