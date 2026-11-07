package com.marithe.sysfact.service;

import com.marithe.sysfact.model.Articulo;
import java.util.List;

public interface ArticuloService {

    List<Articulo> getList(Articulo obj);

    Articulo getById(Long id);

    Articulo insert(Articulo obj);

    Articulo update(Articulo obj);

    void delete(Long id);

}