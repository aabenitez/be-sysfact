package com.marithe.sysfact.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.marithe.sysfact.dao.ParametroDao;
import com.marithe.sysfact.model.Parametro;
import com.marithe.sysfact.service.ParametroService;

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

    @Override
    public Parametro findById(Long id) {
        return dao.findById(id);
    }

    @Override
    public Parametro insert(Parametro obj) {
        dao.insert(obj);
        return obj;
    }

    @Override
    public void update(Parametro obj) {
        dao.update(obj);
    }

    @Override
    public void delete(Long id) {
        dao.delete(id);
    }

}
