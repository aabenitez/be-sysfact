package com.marithe.sysfact.dao;

import com.marithe.sysfact.model.UnidadMedida;

import java.util.List;

public interface UnidadMedidaDao {

	List<UnidadMedida> getList(UnidadMedida obj);

	UnidadMedida getById(Long id);

	/** Units with this name, ignoring case (at most one, the name is unique). */
	List<UnidadMedida> findByNombre(String nombre);

	/** How many articles are sold in this unit. */
	long countArticulos(Long id);

	UnidadMedida insert(UnidadMedida obj);

	UnidadMedida update(UnidadMedida obj);

	void delete(Long id);
}
