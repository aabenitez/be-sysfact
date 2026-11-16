package com.marithe.sysfact.service.impl;

import com.marithe.sysfact.dao.ClienteDao;
import com.marithe.sysfact.exception.BusinessLogicException;
import com.marithe.sysfact.model.Cliente;
import com.marithe.sysfact.service.ClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

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
	public Cliente insert(Cliente obj) {
		normalizar(obj);
		validarCiRucUnico(obj);
		return dao.insert(obj);
	}

	@Override
	public void update(Cliente obj) {
		normalizar(obj);
		validarCiRucUnico(obj);
		dao.update(obj);
	}

	@Override
	public void delete(Long id) {
		long ventas = dao.countVentas(id);
		if (ventas > 0) {
			throw new BusinessLogicException("No se puede eliminar el cliente porque tiene "
					+ ventas + (ventas == 1 ? " venta registrada." : " ventas registradas."));
		}
		dao.delete(id);
	}

	/** The CI/RUC identifies the client on invoices, so two clients cannot share it. */
	private void validarCiRucUnico(Cliente obj) {
		for (Cliente otro : dao.findByCiRuc(obj.getCiRuc())) {
			if (obj.getId() == null || !otro.getId().equals(obj.getId())) {
				throw new BusinessLogicException("Ya existe un cliente con la cédula o RUC " + obj.getCiRuc()
						+ ": " + otro.getNombres().trim() + " " + otro.getApellidos().trim() + ".");
			}
		}
	}

	/** Trims text, lowercases the email and stores an empty email as null. */
	private static void normalizar(Cliente obj) {
		obj.setNombres(obj.getNombres().trim());
		obj.setApellidos(obj.getApellidos().trim());
		obj.setCiRuc(obj.getCiRuc().trim().toUpperCase(Locale.ROOT));
		obj.setTelefono(obj.getTelefono().trim());
		if (obj.getEmail() != null) {
			String email = obj.getEmail().trim().toLowerCase(Locale.ROOT);
			obj.setEmail(email.isEmpty() ? null : email);
		}
	}
}
