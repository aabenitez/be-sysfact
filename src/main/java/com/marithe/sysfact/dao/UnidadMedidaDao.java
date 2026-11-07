package com.marithe.sysfact.dao;

import com.marithe.sysfact.model.UnidadMedida;

import java.util.List;

public interface UnidadMedidaDao {

	List<UnidadMedida> getList(UnidadMedida obj);

	UnidadMedida getById(Long id);

	UnidadMedida insert(UnidadMedida obj);

	UnidadMedida update(UnidadMedida obj);

	void delete(Long id);
}
