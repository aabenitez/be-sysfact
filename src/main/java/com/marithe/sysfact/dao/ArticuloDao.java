package com.marithe.sysfact.dao;

import com.marithe.sysfact.model.Articulo;
import java.util.List;

public interface ArticuloDao {

    List<Articulo> getList(Articulo obj);

    Articulo getById(Long id);

    Articulo insert(Articulo obj);
    Articulo update(Articulo obj);
    void delete(Long id);
}