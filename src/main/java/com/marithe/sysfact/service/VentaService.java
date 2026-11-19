package com.marithe.sysfact.service;

import com.marithe.sysfact.model.Auditoria;
import com.marithe.sysfact.model.Venta;
import java.util.List;

public interface VentaService {
    List<Venta> getList(Venta obj);
    Venta getById(Long id);
    Venta insert(Venta obj);

    /**
     * Replaces the client, payment terms and lines of an issued sale (stock is adjusted). Returns null
     * when the sale does not exist.
     */
    Venta update(Long id, Venta obj);

    /**
     * Cancels a sale instead of deleting it: it keeps its number, with who cancelled it, when and why,
     * and its stock is returned. Returns null when the sale does not exist.
     */
    Venta anular(Long id, String motivo);

    /** Audit entries of a sale, oldest first. */
    List<Auditoria> historial(Long id);
}
