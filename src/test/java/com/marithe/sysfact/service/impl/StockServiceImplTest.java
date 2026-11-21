package com.marithe.sysfact.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.marithe.sysfact.dao.ArticuloDao;
import com.marithe.sysfact.dao.StockDao;
import com.marithe.sysfact.exception.BusinessLogicException;
import com.marithe.sysfact.model.Articulo;
import com.marithe.sysfact.model.Auditoria;
import com.marithe.sysfact.model.MovimientoStock;
import com.marithe.sysfact.model.Usuario;
import com.marithe.sysfact.security.UsuarioActual;
import com.marithe.sysfact.service.AuditoriaService;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class StockServiceImplTest {

	private static final long MACETA = 10L;
	private static final long TIERRA = 11L;

	@Mock
	private StockDao dao;
	@Mock
	private ArticuloDao articuloDao;
	@Mock
	private AuditoriaService auditoria;
	@Mock
	private UsuarioActual usuarioActual;
	@InjectMocks
	private StockServiceImpl service;

	@BeforeEach
	void setUp() {
		Usuario u = new Usuario();
		u.setId(2L);
		u.setNombres("Carga ");
		u.setApellidos(" Dor");
		when(usuarioActual.get()).thenReturn(u);
		Articulo maceta = new Articulo();
		maceta.setId(MACETA);
		maceta.setNombre("Maceta");
		when(articuloDao.getById(MACETA)).thenReturn(maceta);
	}

	private MovimientoStock movimientoGuardado() {
		ArgumentCaptor<MovimientoStock> c = ArgumentCaptor.forClass(MovimientoStock.class);
		verify(dao).insertMovimiento(c.capture());
		return c.getValue();
	}

	@Test
	void ingresoSumaYRegistraMovimientoYAuditoria() {
		when(dao.existenciaParaActualizar(MACETA)).thenReturn(-2L);
		when(dao.sumar(MACETA, 20L)).thenReturn(18L);

		service.ingresar(MACETA, 20L, 3L, "  Remito 455 ");

		MovimientoStock m = movimientoGuardado();
		assertEquals(MovimientoStock.INGRESO, m.getTipo());
		assertEquals(20L, m.getCantidad());
		assertEquals(18L, m.getExistencia());
		assertEquals(3L, m.getProveedor().getId());
		assertEquals("Remito 455", m.getMotivo());
		assertEquals("Carga Dor", m.getUsuarioNombre());
		verify(auditoria).registrar(eq(Auditoria.INGRESO), eq(Auditoria.STOCK), eq(MACETA),
				eq("Maceta: +20 (-2 → 18). Remito 455"), any(), any());
	}

	@Test
	void ingresoRechazaCantidadNoPositiva() {
		assertThrows(BusinessLogicException.class, () -> service.ingresar(MACETA, 0L, null, null));
		assertThrows(BusinessLogicException.class, () -> service.ingresar(MACETA, -5L, null, null));
		assertThrows(BusinessLogicException.class, () -> service.ingresar(MACETA, null, null, null));
		verify(dao, never()).sumar(anyLong(), anyLong());
	}

	@Test
	void ingresoDeArticuloInexistente() {
		BusinessLogicException e = assertThrows(BusinessLogicException.class, () -> service.ingresar(99L, 1L, null, null));
		assertTrue(e.getMessage().contains("no existe"));
	}

	@Test
	void ajusteFijaLaExistenciaContadaConLaDiferencia() {
		when(dao.existenciaParaActualizar(MACETA)).thenReturn(7L);
		when(dao.sumar(MACETA, -2L)).thenReturn(5L);

		service.ajustar(MACETA, 5L, "Se rompieron dos");

		MovimientoStock m = movimientoGuardado();
		assertEquals(MovimientoStock.AJUSTE, m.getTipo());
		assertEquals(-2L, m.getCantidad());
		assertEquals(5L, m.getExistencia());
		verify(auditoria).registrar(eq(Auditoria.AJUSTE), eq(Auditoria.STOCK), eq(MACETA),
				eq("Maceta: 7 → 5 (-2). Motivo: Se rompieron dos"), any(), any());
	}

	@Test
	void ajusteExigeMotivoYNoAceptaNegativos() {
		assertThrows(BusinessLogicException.class, () -> service.ajustar(MACETA, 5L, "  "));
		assertThrows(BusinessLogicException.class, () -> service.ajustar(MACETA, -1L, "Conteo anual"));
		verify(dao, never()).sumar(anyLong(), anyLong());
	}

	@Test
	void ajusteSinDiferenciaSeRechaza() {
		when(dao.existenciaParaActualizar(MACETA)).thenReturn(5L);
		BusinessLogicException e = assertThrows(BusinessLogicException.class,
				() -> service.ajustar(MACETA, 5L, "Conteo anual"));
		assertTrue(e.getMessage().contains("ya es 5"));
		verify(dao, never()).insertMovimiento(any());
		verify(auditoria, never()).registrar(anyString(), anyString(), anyLong(), anyString(), any(), any());
	}

	@Test
	void ventaMueveSoloLosArticulosConCambio() {
		Map<Long, Long> cambios = new LinkedHashMap<>();
		cambios.put(MACETA, -3L);
		cambios.put(TIERRA, 0L);
		cambios.put(12L, 2L);
		when(dao.sumar(MACETA, -3L)).thenReturn(4L);
		when(dao.sumar(12L, 2L)).thenReturn(9L);

		service.moverPorVenta(30L, cambios, "Edición de la factura N.º 000030");

		ArgumentCaptor<MovimientoStock> c = ArgumentCaptor.forClass(MovimientoStock.class);
		verify(dao, times(2)).insertMovimiento(c.capture());
		List<MovimientoStock> movs = c.getAllValues();
		assertEquals(MovimientoStock.VENTA, movs.get(0).getTipo());
		assertEquals(4L, movs.get(0).getExistencia());
		assertEquals(30L, movs.get(0).getVentaId());
		assertEquals(MovimientoStock.DEVOLUCION, movs.get(1).getTipo());
		assertEquals(2L, movs.get(1).getCantidad());
		assertNull(movs.get(1).getProveedor());
		verify(dao, never()).sumar(TIERRA, 0L);
	}
}
