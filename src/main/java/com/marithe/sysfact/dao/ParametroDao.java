package py.com.ventasjdbc.dao;

import py.com.ventasjdbc.model.Parametro;

import java.util.List;

public interface ParametroDao {
    List<Parametro> getAll();

    Parametro findByCodigo(String codigo);
}
