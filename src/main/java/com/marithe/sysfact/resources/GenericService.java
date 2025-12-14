package com.marithe.sysfact.resources;

import com.marithe.sysfact.model.BaseEntity;

import java.util.List;

public interface GenericService<T extends BaseEntity> {
    List<T> getAll(T obj);

    T findById(Long id);

    T insert(T obj);

    T update(T obj);

    void delete(Long id);
}
