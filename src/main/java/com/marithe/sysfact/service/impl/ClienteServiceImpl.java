package com.marithe.sysfact.service.impl;

import com.marithe.sysfact.dao.ClienteDao;
import com.marithe.sysfact.exception.BusinessLogicException;
import com.marithe.sysfact.model.Auditoria;
import com.marithe.sysfact.model.Cliente;
import com.marithe.sysfact.service.AuditoriaService;
import com.marithe.sysfact.service.ClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@Transactional
public class ClienteServiceImpl implements ClienteService {

	@Autowired
	private ClienteDao dao;

	@Autowired
	private AuditoriaService auditoria;

	@Override
	public List<Cliente> getAll(Cliente obj) {	return dao.getAll(obj); }

	@Override
	public Cliente findById(Long id) { return dao.findById(id); }

	@Override
	public Cliente insert(Cliente obj) {
		normalizar(obj);
		validarCiRucUnico(obj);
		Cliente creado = dao.insert(obj);
		auditoria.registrar(Auditoria.CREAR, Auditoria.CLIENTE, creado.getId(), describir(creado), null, creado);
		return creado;
	}

	@Override
	public void update(Cliente obj) {
		normalizar(obj);
		validarCiRucUnico(obj);
		Cliente antes = dao.findById(obj.getId());
		dao.update(obj);
		auditoria.registrar(Auditoria.EDITAR, Auditoria.CLIENTE, obj.getId(),
				describir(obj) + ": " + cambios(antes, obj), antes, obj);
	}

	@Override
	public void delete(Long id) {
		long ventas = dao.countVentas(id);
		if (ventas > 0) {
			throw new BusinessLogicException("No se puede eliminar el cliente porque tiene "
					+ ventas + (ventas == 1 ? " venta registrada." : " ventas registradas."));
		}
		Cliente antes = dao.findById(id);
		dao.delete(id);
		auditoria.registrar(Auditoria.ELIMINAR, Auditoria.CLIENTE, id, describir(antes), antes, null);
	}

	private static String describir(Cliente c) {
		if (c == null) {
			return "Cliente";
		}
		return "Cliente " + c.getNombres().trim() + " " + c.getApellidos().trim() + " (CI/RUC " + c.getCiRuc().trim() + ")";
	}

	static String cambios(Cliente antes, Cliente despues) {
		if (antes == null) {
			return "datos actualizados";
		}
		List<String> cambios = new ArrayList<>();
		campo(cambios, "nombres", antes.getNombres(), despues.getNombres());
		campo(cambios, "apellidos", antes.getApellidos(), despues.getApellidos());
		campo(cambios, "CI/RUC", antes.getCiRuc(), despues.getCiRuc());
		campo(cambios, "teléfono", antes.getTelefono(), despues.getTelefono());
		campo(cambios, "correo", antes.getEmail(), despues.getEmail());
		return cambios.isEmpty() ? "sin cambios en los datos" : String.join("; ", cambios);
	}

	private static void campo(List<String> cambios, String nombre, String antes, String despues) {
		String a = antes == null ? "" : antes.trim();
		String d = despues == null ? "" : despues.trim();
		if (!a.equals(d)) {
			cambios.add(nombre + " \"" + a + "\" → \"" + d + "\"");
		}
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
