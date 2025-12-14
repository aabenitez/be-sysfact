package com.marithe.sysfact.service;

import com.marithe.sysfact.model.Parametro;

import java.util.List;

public interface ParametroService {
    List<Parametro> getAll();

    Parametro findByCodigo(String codigo);
}
