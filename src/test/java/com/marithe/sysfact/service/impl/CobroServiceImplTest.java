package com.marithe.sysfact.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.marithe.sysfact.dao.CobroDao;
import com.marithe.sysfact.dao.ParametroDao;
import com.marithe.sysfact.dao.VentaDao;
import com.marithe.sysfact.exception.BusinessLogicException;
import com.marithe.sysfact.model.Auditoria;
import com.marithe.sysfact.model.Cobro;
import com.marithe.sysfact.model.CuentaPorCobrar;
import com.marithe.sysfact.model.Parametro;
import com.marithe.sysfact.model.Usuario;
import com.marithe.sysfact.model.Venta;
import com.marithe.sysfact.security.UsuarioActual;
import com.marithe.sysfact.service.AuditoriaService;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CobroServiceImplTest {

	@Mock
	private CobroDao dao;
	@Mock
	private VentaDao ventaDao;
	@Mock
	private ParametroDao parametroDao;
	@Mock
	private AuditoriaService auditoria;
	@Mock
	private UsuarioActual usuarioActual;
	@InjectMocks
	private CobroServiceImpl service;

	private final Usuario cajero = new Usuario();

	@BeforeEach
	void setUp() {
		cajero.setId(3L);
		cajero.setNombres("Caja");
		cajero.setApellidos("Uno");
		when(usuarioActual.get()).thenReturn(cajero);
		when(usuarioActual.esAdministrador()).thenReturn(false);
		when(dao.anular(anyLong(), anyLong(), anyString(), anyString())).thenReturn(true);
		doAnswer(i -> {
			Cobro c = i.getArgument(0);
			c.setId(70L);
			c.setFecha(new Date());
			c.setEstado(Cobro.VIGENTE);
			return null;
		}).when(dao).insert(any());
	}

	/** Venta 20: credit, Gs. 100.000, of which cobrado is already paid. */
	private Venta venta(String condicion, String estado, long cobrado) {
		Venta v = new Venta();
		v.setId(20L);
		v.setCondicionVenta(condicion);
		v.setEstado(estado);
		v.setTotalFactura(new BigDecimal("100000"));
		when(ventaDao.getById(20L)).thenReturn(v);
		when(dao.cobradoParaActualizar(20L)).thenReturn(BigDecimal.valueOf(cobrado));
		return v;
	}

	private static BigDecimal gs(long n) {
		return BigDecimal.valueOf(n);
	}

	private void falla(String esperado, Runnable r) {
		BusinessLogicException e = assertThrows(BusinessLogicException.class, r::run);
		assertTrue(e.getMessage().contains(esperado), e.getMessage());
	}

	@Test
	void registersAPartialPaymentAndAuditsTheBalance() {
		venta("CRE", Venta.EMITIDA, 30000);

		Cobro c = service.registrar(20L, gs(50000), "EFECTIVO", "  Recibo 12 ");

		ArgumentCaptor<Cobro> guardado = ArgumentCaptor.forClass(Cobro.class);
		verify(dao).insert(guardado.capture());
		assertEquals(gs(50000), guardado.getValue().getMonto());
		assertEquals("Recibo 12", guardado.getValue().getReferencia());
		assertEquals("Caja Uno", guardado.getValue().getUsuarioNombre());
		assertEquals(70L, c.getId());
		ArgumentCaptor<String> detalle = ArgumentCaptor.forClass(String.class);
		verify(auditoria).registrar(eq(Auditoria.CREAR), eq(Auditoria.COBRO), eq(70L), detalle.capture(), isNull(), any());
		assertEquals("Factura N.º 000020: cobró Gs. 50.000 en efectivo (Recibo 12). Saldo Gs. 70.000 → Gs. 20.000",
				detalle.getValue());
	}

	@Test
	void canPayExactlyTheBalance() {
		venta("CRE", Venta.EMITIDA, 30000);
		service.registrar(20L, gs(70000), "TRANSFERENCIA", null);
		verify(dao).insert(any());
	}

	@Test
	void cannotPayMoreThanTheBalance() {
		venta("CRE", Venta.EMITIDA, 30000);
		falla("El saldo de la factura N.º 000020 es Gs. 70.000", () -> service.registrar(20L, gs(70001), "EFECTIVO", null));
		verify(dao, never()).insert(any());
	}

	@Test
	void paidSaleTakesNoMorePayments() {
		venta("CRE", Venta.EMITIDA, 100000);
		falla("ya está pagada", () -> service.registrar(20L, gs(1000), "EFECTIVO", null));
	}

	@Test
	void onlyValidCreditSalesTakePayments() {
		venta("CON", Venta.EMITIDA, 0);
		falla("es al contado", () -> service.registrar(20L, gs(1000), "EFECTIVO", null));
		venta("CRE", Venta.ANULADA, 0);
		falla("está anulada", () -> service.registrar(20L, gs(1000), "EFECTIVO", null));
		verify(dao, never()).insert(any());
	}

	@Test
	void validatesAmountAndPaymentMethod() {
		venta("CRE", Venta.EMITIDA, 0);
		falla("mayor que cero", () -> service.registrar(20L, gs(0), "EFECTIVO", null));
		falla("mayor que cero", () -> service.registrar(20L, null, "EFECTIVO", null));
		falla("sin decimales", () -> service.registrar(20L, new BigDecimal("1000.50"), "EFECTIVO", null));
		falla("forma de pago", () -> service.registrar(20L, gs(1000), "BITCOIN", null));
		verify(dao, never()).insert(any());
	}

	@Test
	void missingSaleReturnsNull() {
		assertNull(service.registrar(404L, gs(1000), "EFECTIVO", null));
	}

	private Cobro cobro(long id, long ventaId, long monto, LocalDate dia, String estado) {
		Cobro c = new Cobro();
		c.setId(id);
		c.setVentaId(ventaId);
		c.setMonto(gs(monto));
		c.setEstado(estado);
		c.setFecha(Date.from(dia.atTime(10, 0).atZone(ZoneId.systemDefault()).toInstant()));
		when(dao.getById(id)).thenReturn(c);
		return c;
	}

	@Test
	void cancelsAPaymentWithAReason() {
		venta("CRE", Venta.EMITIDA, 50000);
		cobro(5L, 20L, 50000, LocalDate.now(), Cobro.VIGENTE);

		service.anular(20L, 5L, "  Se cargó dos veces ");

		verify(dao).anular(5L, 3L, "Caja Uno", "Se cargó dos veces");
		ArgumentCaptor<String> detalle = ArgumentCaptor.forClass(String.class);
		verify(auditoria).registrar(eq(Auditoria.ANULAR), eq(Auditoria.COBRO), eq(5L), detalle.capture(), any(), any());
		assertTrue(detalle.getValue().contains("anuló el cobro de Gs. 50.000"), detalle.getValue());
		assertTrue(detalle.getValue().contains("Saldo Gs. 50.000 → Gs. 100.000"), detalle.getValue());
	}

	@Test
	void cancelNeedsAReasonAndAValidPaymentOfThatSale() {
		venta("CRE", Venta.EMITIDA, 50000);
		cobro(5L, 20L, 50000, LocalDate.now(), Cobro.VIGENTE);
		cobro(6L, 20L, 10000, LocalDate.now(), Cobro.ANULADO);
		cobro(7L, 99L, 10000, LocalDate.now(), Cobro.VIGENTE);
		falla("motivo", () -> service.anular(20L, 5L, "abc"));
		falla("ya está anulado", () -> service.anular(20L, 6L, "Otro motivo largo"));
		assertNull(service.anular(20L, 7L, "Es de otra factura"));
		verify(dao, never()).anular(anyLong(), anyLong(), anyString(), anyString());
	}

	@Test
	void cashierCannotCancelAPaymentFromAnotherDay() {
		venta("CRE", Venta.EMITIDA, 50000);
		cobro(5L, 20L, 50000, LocalDate.now().minusDays(1), Cobro.VIGENTE);
		falla("días anteriores", () -> service.anular(20L, 5L, "Se cargó dos veces"));
		when(usuarioActual.esAdministrador()).thenReturn(true);
		service.anular(20L, 5L, "Se cargó dos veces");
		verify(dao).anular(5L, 3L, "Caja Uno", "Se cargó dos veces");
	}

	@Test
	void pendingSalesGetDueDateAndDaysPastDue() {
		Parametro p = new Parametro();
		p.setValor("15");
		when(parametroDao.findByCodigo("credito.plazo.dias")).thenReturn(p);
		CuentaPorCobrar vieja = new CuentaPorCobrar();
		vieja.setFecha(Date.from(LocalDate.now().minusDays(20).atTime(9, 0).atZone(ZoneId.systemDefault()).toInstant()));
		CuentaPorCobrar nueva = new CuentaPorCobrar();
		nueva.setFecha(new Date());
		when(dao.pendientes()).thenReturn(new ArrayList<>(List.of(vieja, nueva)));

		List<CuentaPorCobrar> l = service.pendientes();

		assertEquals(5, l.get(0).getDiasAtraso());
		assertEquals(0, l.get(1).getDiasAtraso());
		assertEquals(LocalDate.now().plusDays(15).toString(), l.get(1).getVencimiento());
	}

	@Test
	void creditTermFallsBackTo30Days() {
		assertEquals(30, service.plazoDias());
		Parametro p = new Parametro();
		p.setValor("treinta");
		when(parametroDao.findByCodigo("credito.plazo.dias")).thenReturn(p);
		assertEquals(30, service.plazoDias());
	}
}
