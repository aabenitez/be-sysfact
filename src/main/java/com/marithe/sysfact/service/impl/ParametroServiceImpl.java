package py.com.ventasjdbc.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import py.com.ventasjdbc.dao.ParametroDao;
import py.com.ventasjdbc.model.Parametro;
import py.com.ventasjdbc.service.ParametroService;

import java.util.List;

@Service
@Transactional
public class ParametroServiceImpl implements ParametroService {
    @Autowired
    ParametroDao dao;

    @Override
    public List<Parametro> getAll() {
        return dao.getAll();
    }

    @Override
    public Parametro findByCodigo(String codigo) {
        return dao.findByCodigo(codigo);
    }
}
