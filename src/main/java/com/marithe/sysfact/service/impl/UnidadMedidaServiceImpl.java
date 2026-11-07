package com.marithe.sysfact.service.impl;

import com.marithe.sysfact.dao.UnidadMedidaDao;
import com.marithe.sysfact.model.UnidadMedida;
import com.marithe.sysfact.service.UnidadMedidaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UnidadMedidaServiceImpl implements UnidadMedidaService{
	
	@Autowired
	private UnidadMedidaDao dao;

	@Override
	public List<UnidadMedida> getList(UnidadMedida obj) {
		return dao.getList(obj);
	}

	@Override
	public UnidadMedida getById(Long id) {
		return dao.getById(id);
	}

	@Override
	public UnidadMedida insert(UnidadMedida obj) {
		return dao.insert(obj);
	}

	@Override
	public UnidadMedida update(UnidadMedida obj) {
		return dao.update(obj);
	}

	@Override
	public void delete(Long id) { dao.delete(id); }

}
