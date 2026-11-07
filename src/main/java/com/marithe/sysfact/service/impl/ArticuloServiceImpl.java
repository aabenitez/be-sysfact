package com.marithe.sysfact.service.impl;

import com.marithe.sysfact.dao.ArticuloDao;
import com.marithe.sysfact.model.Articulo;
import com.marithe.sysfact.service.ArticuloService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class ArticuloServiceImpl implements ArticuloService {

    @Autowired
    private ArticuloDao articuloDao;

    @Override
    public List<Articulo> getList(Articulo obj) {
        return articuloDao.getList(obj);
    }

    @Override
    public Articulo getById(Long id) {
        return articuloDao.getById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Articulo insert(Articulo obj) {
        return articuloDao.insert(obj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Articulo update(Articulo obj) {
        return articuloDao.update(obj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        articuloDao.delete(id);
    }
}