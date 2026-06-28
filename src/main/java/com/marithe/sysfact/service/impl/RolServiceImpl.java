package com.marithe.sysfact.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.marithe.sysfact.dao.RolDao;
import com.marithe.sysfact.model.Rol;
import com.marithe.sysfact.service.RolService;

import java.util.List;

@Service
public class RolServiceImpl implements RolService{
	
	@Autowired
	private RolDao dao;

	@Override
	public List<Rol> getList(Rol obj) {
		return dao.getList(obj);
	}

	@Override
	public Rol getById(Long id) {
		return dao.getById(id);
	}

	@Override
	public Rol insert(Rol obj) {
		return dao.insert(obj);
	}

	@Override
	public Rol update(Rol obj) {
		return dao.update(obj);
	}

	@Override
	public void delete(Long id) { dao.delete(id); }

}
