package py.com.ventasjdbc.service;

import java.util.List;

import py.com.ventasjdbc.model.Rol;

public interface RolService {
	
	List<Rol> getList(Rol obj);

	Rol getById(Long id);

	Rol insert(Rol obj);

	Rol update(Rol obj);

	void delete(Long id);


}
