package com.marithe.sysfact.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.marithe.sysfact.dao.UnidadMedidaDao;
import com.marithe.sysfact.exception.BusinessLogicException;
import com.marithe.sysfact.model.Auditoria;
import com.marithe.sysfact.model.UnidadMedida;
import com.marithe.sysfact.service.AuditoriaService;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UnidadMedidaServiceImplTest {

	@Mock
	private UnidadMedidaDao dao;
	@Mock
	private AuditoriaService auditoria;
	@InjectMocks
	private UnidadMedidaServiceImpl service;

	private static UnidadMedida unidad(Long id, String nombre, String plural) {
		UnidadMedida u = new UnidadMedida();
		u.setId(id);
		u.setNombre(nombre);
		u.setPlural(plural);
		return u;
	}

	@Test
	void insertNormalizaYRegistraAuditoria() {
		when(dao.findByNombre("Maceta")).thenReturn(Collections.emptyList());
		when(dao.insert(any())).thenAnswer(inv -> {
			UnidadMedida u = inv.getArgument(0);
			u.setId(7L);
			return u;
		});

		UnidadMedida creada = service.insert(unidad(null, "  Maceta ", " Macetas  "));

		assertEquals("Maceta", creada.getNombre());
		assertEquals("Macetas", creada.getPlural());
		verify(auditoria).registrar(eq(Auditoria.CREAR), eq(Auditoria.UNIDAD_MEDIDA), eq(7L),
				eq("Unidad de medida Maceta (Macetas)"), isNull(), eq(creada));
	}

	@Test
	void insertRechazaNombreRepetido() {
		when(dao.findByNombre("maceta")).thenReturn(Collections.singletonList(unidad(1L, "Maceta", "Macetas")));

		BusinessLogicException e = assertThrows(BusinessLogicException.class,
				() -> service.insert(unidad(null, "maceta", "macetas")));

		assertTrue(e.getMessage().contains("Ya existe una unidad de medida llamada \"Maceta\""));
		verify(dao, never()).insert(any());
	}

	@Test
	void updatePermiteConservarSuPropioNombreYDescribeLosCambios() {
		when(dao.findByNombre("Maceta")).thenReturn(Collections.singletonList(unidad(1L, "Maceta", "Macetas")));
		when(dao.getById(1L)).thenReturn(unidad(1L, "Maceta", "Macetas"));

		service.update(unidad(1L, "Maceta", "Macetones"));

		ArgumentCaptor<String> detalle = ArgumentCaptor.forClass(String.class);
		verify(auditoria).registrar(eq(Auditoria.EDITAR), eq(Auditoria.UNIDAD_MEDIDA), eq(1L), detalle.capture(),
				any(), any());
		assertEquals("Unidad de medida Maceta (Macetones): plural \"Macetas\" → \"Macetones\"", detalle.getValue());
	}

	@Test
	void deleteRechazaUnidadEnUso() {
		when(dao.countArticulos(2L)).thenReturn(3L);

		BusinessLogicException e = assertThrows(BusinessLogicException.class, () -> service.delete(2L));

		assertTrue(e.getMessage().contains("la usan 3 artículos"));
		verify(dao, never()).delete(anyLong());
		verify(auditoria, never()).registrar(anyString(), anyString(), anyLong(), anyString(), any(), any());
	}

	@Test
	void deleteSinUsoBorraYRegistraAuditoria() {
		UnidadMedida antes = unidad(4L, "Bolsa", "Bolsas");
		when(dao.countArticulos(4L)).thenReturn(0L);
		when(dao.getById(4L)).thenReturn(antes);

		service.delete(4L);

		verify(dao).delete(4L);
		verify(auditoria).registrar(eq(Auditoria.ELIMINAR), eq(Auditoria.UNIDAD_MEDIDA), eq(4L),
				eq("Unidad de medida Bolsa (Bolsas)"), eq(antes), isNull());
	}
}
