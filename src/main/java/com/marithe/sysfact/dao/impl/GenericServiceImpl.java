package com.marithe.sysfact.dao.impl;

import com.marithe.sysfact.model.BaseEntity;
import com.marithe.sysfact.resources.GenericDao;
import com.marithe.sysfact.resources.GenericService;

import java.util.List;

public abstract class GenericServiceImpl<T extends BaseEntity> implements GenericService<T> {
    GenericDao<T> dao;

    public GenericServiceImpl(GenericDao<T> dao) {
        this.dao = dao;
    }

    @Override
    public List<T> getAll(T obj) {
        return dao.getAll(obj);
    }

    @Override
    public T findById(Long id) {
        return dao.findById(id);
    }

    @Override
    public T insert(T obj) {
        return dao.insert(obj);
    }

    @Override
    public T update(T obj) {
        return dao.update(obj);
    }

    @Override
    public void delete(Long id) {
        dao.delete(id);
    }
}
