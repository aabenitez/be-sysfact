package py.com.ventasjdbc.service;

import py.com.ventasjdbc.model.Parametro;

import java.util.List;

public interface ParametroService {
    List<Parametro> getAll();

    Parametro findByCodigo(String codigo);
}
