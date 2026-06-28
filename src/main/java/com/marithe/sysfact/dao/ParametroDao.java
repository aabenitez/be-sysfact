package com.marithe.sysfact.dao;

import com.marithe.sysfact.model.Parametro;

import java.util.List;

public interface ParametroDao {
    List<Parametro> getAll();

    Parametro findByCodigo(String codigo);
    Parametro findById(Long id);

    void insert(Parametro obj);
    void update(Parametro obj);
    void delete(Long id);
}
