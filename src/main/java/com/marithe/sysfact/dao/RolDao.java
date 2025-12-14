package com.marithe.sysfact.dao;

import com.marithe.sysfact.model.Rol;

import java.util.List;

public interface RolDao {

	List<Rol> getList(Rol obj);

	Rol getById(Long id);

	Rol insert(Rol obj);

	Rol update(Rol obj);

	void delete(Long id);
}
