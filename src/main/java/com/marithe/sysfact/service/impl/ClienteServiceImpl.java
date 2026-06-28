package com.marithe.sysfact.service.impl;

import com.marithe.sysfact.dao.ClienteDao;
import com.marithe.sysfact.model.Cliente;
import com.marithe.sysfact.service.ClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ClienteServiceImpl implements ClienteService {
	
	@Autowired
	private ClienteDao dao;

	@Override
	public List<Cliente> getAll(Cliente obj) {	return dao.getAll(obj); }

	@Override
	public Cliente findById(Long id) { return dao.findById(id); }

	@Override
	public Cliente insert(Cliente obj) { return dao.insert(obj); }

	@Override
	public void update(Cliente obj) { dao.update(obj); }

	@Override
	public void delete(Long id) { dao.delete(id); }

}
