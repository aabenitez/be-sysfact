package com.marithe.sysfact.service.impl;

import com.marithe.sysfact.dao.UnidadMedidaDao;
import com.marithe.sysfact.exception.BusinessLogicException;
import com.marithe.sysfact.model.Auditoria;
import com.marithe.sysfact.model.UnidadMedida;
import com.marithe.sysfact.service.AuditoriaService;
import com.marithe.sysfact.service.UnidadMedidaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class UnidadMedidaServiceImpl implements UnidadMedidaService {

	@Autowired
	private UnidadMedidaDao dao;

	@Autowired
	private AuditoriaService auditoria;

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
		normalizar(obj);
		validarNombreUnico(obj);
		UnidadMedida creada = dao.insert(obj);
		auditoria.registrar(Auditoria.CREAR, Auditoria.UNIDAD_MEDIDA, creada.getId(), describir(creada), null, creada);
		return creada;
	}

	@Override
	public UnidadMedida update(UnidadMedida obj) {
		normalizar(obj);
		validarNombreUnico(obj);
		UnidadMedida antes = dao.getById(obj.getId());
		dao.update(obj);
		auditoria.registrar(Auditoria.EDITAR, Auditoria.UNIDAD_MEDIDA, obj.getId(),
				describir(obj) + ": " + cambios(antes, obj), antes, obj);
		return obj;
	}

	@Override
	public void delete(Long id) {
		long articulos = dao.countArticulos(id);
		if (articulos > 0) {
			throw new BusinessLogicException("No se puede eliminar la unidad de medida porque la usa"
					+ (articulos == 1 ? " 1 artículo." : "n " + articulos + " artículos.")
					+ " Cambiá la unidad de esos artículos antes de eliminarla.");
		}
		UnidadMedida antes = dao.getById(id);
		dao.delete(id);
		auditoria.registrar(Auditoria.ELIMINAR, Auditoria.UNIDAD_MEDIDA, id, describir(antes), antes, null);
	}

	private static String describir(UnidadMedida u) {
		return u == null ? "Unidad de medida" : "Unidad de medida " + u.getNombre() + " (" + u.getPlural() + ")";
	}

	static String cambios(UnidadMedida antes, UnidadMedida despues) {
		if (antes == null) {
			return "datos actualizados";
		}
		List<String> cambios = new ArrayList<>();
		campo(cambios, "nombre", antes.getNombre(), despues.getNombre());
		campo(cambios, "plural", antes.getPlural(), despues.getPlural());
		return cambios.isEmpty() ? "sin cambios en los datos" : String.join("; ", cambios);
	}

	private static void campo(List<String> cambios, String nombre, String antes, String despues) {
		String a = antes == null ? "" : antes.trim();
		String d = despues == null ? "" : despues.trim();
		if (!a.equals(d)) {
			cambios.add(nombre + " \"" + a + "\" → \"" + d + "\"");
		}
	}

	/** The name is unique in the database; checked here to give a clear message. */
	private void validarNombreUnico(UnidadMedida obj) {
		for (UnidadMedida otra : dao.findByNombre(obj.getNombre())) {
			if (obj.getId() == null || !otra.getId().equals(obj.getId())) {
				throw new BusinessLogicException("Ya existe una unidad de medida llamada \"" + otra.getNombre() + "\".");
			}
		}
	}

	/** Trims and collapses inner spaces. */
	private static void normalizar(UnidadMedida obj) {
		obj.setNombre(obj.getNombre().trim().replaceAll("\\s+", " "));
		obj.setPlural(obj.getPlural().trim().replaceAll("\\s+", " "));
	}
}
