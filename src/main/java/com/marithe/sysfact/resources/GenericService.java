package py.com.ventasjdbc.resources;

import py.com.ventasjdbc.model.BaseEntity;

import java.util.List;

public interface GenericService<T extends BaseEntity> {
    List<T> getAll(T obj);

    T findById(Long id);

    T insert(T obj);

    T update(T obj);

    void delete(Long id);
}
