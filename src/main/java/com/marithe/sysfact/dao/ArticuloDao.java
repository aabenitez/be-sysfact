package com.marithe.sysfact.dao;

import com.marithe.sysfact.model.Articulo;
import java.util.List;

public interface ArticuloDao {

    List<Articulo> getList(Articulo obj);

    Articulo getById(Long id);

    Articulo insert(Articulo obj);
    Articulo update(Articulo obj);
    void delete(Long id);

    /** Sets (or clears, with null) the stored photo file name. */
    void updateImagen(Long id, String nombreImagen);

    /** Crea la fila de stock del artículo con existencia 0 (si no existe). */
    void insertStock(Long articuloId);

    void deleteStock(Long articuloId);

    /** Cantidad de líneas de venta que usan el artículo. */
    long countVentas(Long articuloId);
}