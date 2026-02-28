package com.marithe.sysfact.service;

import com.marithe.sysfact.model.Parametro;

import java.util.List;

public interface ParametroService {
    List<Parametro> getAll();

    Parametro findByCodigo(String codigo);

    Parametro findById(Long id); // Necesario para validar antes de actualizar

    Parametro insert(Parametro obj);
    void update(Parametro obj);
    void delete(Long id);
}
