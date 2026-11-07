package com.marithe.sysfact.service;

import com.marithe.sysfact.model.UnidadMedida;

import java.util.List;

public interface UnidadMedidaService {
	
	List<UnidadMedida> getList(UnidadMedida obj);

	UnidadMedida getById(Long id);

	UnidadMedida insert(UnidadMedida obj);

	UnidadMedida update(UnidadMedida obj);

	void delete(Long id);

}
