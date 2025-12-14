package py.com.ventasjdbc.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import py.com.ventasjdbc.dao.RolDao;
import py.com.ventasjdbc.model.Rol;
import py.com.ventasjdbc.service.RolService;


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
	public void delete(Long id) {
		dao.delete(id);
		
	}

}
