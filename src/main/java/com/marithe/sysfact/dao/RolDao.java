package py.com.ventasjdbc.dao;

import java.util.List;

import py.com.ventasjdbc.model.Rol;

public interface RolDao {

	List<Rol> getList(Rol obj);

	Rol getById(Long id);

	Rol insert(Rol obj);

	Rol update(Rol obj);

	void delete(Long id);
}
