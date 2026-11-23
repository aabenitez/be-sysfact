package com.marithe.sysfact.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.marithe.sysfact.dao.CajaDao;
import com.marithe.sysfact.exception.BusinessLogicException;
import com.marithe.sysfact.model.Auditoria;
import com.marithe.sysfact.model.CajaCierre;
import com.marithe.sysfact.model.CajaMovimiento;
import com.marithe.sysfact.model.CajaResumen;
import com.marithe.sysfact.model.Usuario;
import com.marithe.sysfact.security.UsuarioActual;
import com.marithe.sysfact.service.AuditoriaService;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CajaServiceImplTest {

	@Mock
	private CajaDao dao;
	@Mock
	private AuditoriaService auditoria;
	@Mock
	private UsuarioActual usuarioActual;
	@InjectMocks
	private CajaServiceImpl service;

	private final Usuario cajero = new Usuario();
	private final LocalDate hoy = LocalDate.now();
	/** Closings the fake DAO holds, so a closing shows up in the summary returned after saving it. */
	private final List<CajaCierre> cierres = new ArrayList<>();

	@BeforeEach
	void setUp() {
		cajero.setId(3L);
		cajero.setNombres("Caja");
		cajero.setApellidos("Uno");
		when(usuarioActual.get()).thenReturn(cajero);
		when(usuarioActual.esAdministrador()).thenReturn(false);
		// Today: two cash sales (cash 50.000, card 30.000) and two payments (cash 20.000, transfer 15.000).
		when(dao.ventasContado(hoy)).thenReturn(new ArrayList<>(List.of(
				mov(CajaMovimiento.VENTA, 1L, 9, "50000", "EFECTIVO"),
				mov(CajaMovimiento.VENTA, 2L, 11, "30000", "TARJETA"))));
		when(dao.cobros(hoy)).thenReturn(new ArrayList<>(List.of(
				mov(CajaMovimiento.COBRO, 5L, 10, "20000", "EFECTIVO"),
				mov(CajaMovimiento.COBRO, 6L, 12, "15000", "TRANSFERENCIA"))));
		when(dao.cierres(any())).thenAnswer(i -> new ArrayList<>(cierres));
		doAnswer(i -> {
			CajaCierre c = i.getArgument(0);
			c.setId(40L);
			c.setEstado(CajaCierre.VIGENTE);
			c.setCreado(new Date());
			cierres.add(0, c);
			return null;
		}).when(dao).insertCierre(any());
	}

	private CajaMovimiento mov(String tipo, long id, int hora, String monto, String forma) {
		CajaMovimiento m = new CajaMovimiento();
		m.setTipo(tipo);
		m.setId(id);
		m.setVentaId(id);
		m.setFecha(Date.from(hoy.atTime(hora, 0).atZone(ZoneId.systemDefault()).toInstant()));
		m.setMonto(new BigDecimal(monto));
		m.setFormaPago(forma);
		return m;
	}

	private static BigDecimal gs(long n) {
		return BigDecimal.valueOf(n);
	}

	private static CajaResumen.PorForma forma(CajaResumen r, String f) {
		return r.getPorForma().stream().filter(p -> p.getFormaPago().equals(f)).findFirst().orElseThrow();
	}

	@Test
	void summaryAddsUpByPaymentMethodInTimeOrder() {
		CajaResumen r = service.resumen(null);

		assertEquals(hoy.toString(), r.getFecha());
		assertEquals(0, gs(80000).compareTo(r.getTotalVentas()));
		assertEquals(0, gs(35000).compareTo(r.getTotalCobros()));
		assertEquals(0, gs(115000).compareTo(r.getTotal()));
		assertEquals(0, gs(70000).compareTo(r.getEfectivo()));
		assertEquals(5, r.getPorForma().size());
		assertEquals(0, gs(70000).compareTo(forma(r, "EFECTIVO").getTotal()));
		assertEquals(2, forma(r, "EFECTIVO").getCantidad());
		assertEquals(0, gs(0).compareTo(forma(r, "CHEQUE").getTotal()));
		assertEquals(List.of(1L, 5L, 2L, 6L), r.getMovimientos().stream().map(CajaMovimiento::getId).collect(Collectors.toList()));
		assertNull(r.getCierre());
	}

	@Test
	void cashierOnlyWorksWithTodaysCash() {
		BusinessLogicException e = assertThrows(BusinessLogicException.class,
				() -> service.resumen(hoy.minusDays(1)));
		assertTrue(e.getMessage().contains("Administrador"));
		assertThrows(BusinessLogicException.class,
				() -> service.cerrar(hoy.minusDays(1), BigDecimal.ZERO, gs(70000), null));
	}

	@Test
	void administratorSeesPastDaysButNotFutureOnes() {
		when(usuarioActual.esAdministrador()).thenReturn(true);
		assertNotNull(service.resumen(hoy.minusDays(3)));
		assertThrows(BusinessLogicException.class, () -> service.resumen(hoy.plusDays(1)));
	}

	@Test
	void closingWithoutDifferenceSavesTheTotalsAndAudits() {
		CajaResumen r = service.cerrar(null, gs(100000), gs(170000), "  ");

		ArgumentCaptor<CajaCierre> c = ArgumentCaptor.forClass(CajaCierre.class);
		verify(dao).insertCierre(c.capture());
		CajaCierre cierre = c.getValue();
		assertEquals(hoy.toString(), cierre.getFecha());
		assertEquals(0, gs(170000).compareTo(cierre.getEfectivoEsperado()));
		assertEquals(0, gs(0).compareTo(cierre.getDiferencia()));
		assertEquals(0, gs(80000).compareTo(cierre.getTotalVentas()));
		assertEquals(0, gs(35000).compareTo(cierre.getTotalCobros()));
		assertNull(cierre.getObservacion());
		assertEquals("Caja Uno", cierre.getUsuarioNombre());
		assertEquals(40L, r.getCierre().getId());
		verify(auditoria).registrar(eq(Auditoria.CREAR), eq(Auditoria.CAJA), eq(40L),
				eq("Cierre de caja del " + String.format("%02d/%02d/%d", hoy.getDayOfMonth(), hoy.getMonthValue(),
						hoy.getYear()) + ": efectivo esperado Gs. 170.000, contado Gs. 170.000 (sin diferencia)."
						+ " Entró Gs. 115.000 en total."),
				isNull(), any());
	}

	@Test
	void aDifferenceNeedsAnExplanation() {
		BusinessLogicException e = assertThrows(BusinessLogicException.class,
				() -> service.cerrar(null, BigDecimal.ZERO, gs(65000), ""));
		assertTrue(e.getMessage().contains("faltan Gs. 5.000"), e.getMessage());
		verify(dao, never()).insertCierre(any());

		service.cerrar(null, BigDecimal.ZERO, gs(65000), "Se dio mal un vuelto");
		ArgumentCaptor<CajaCierre> c = ArgumentCaptor.forClass(CajaCierre.class);
		verify(dao).insertCierre(c.capture());
		assertEquals(0, gs(-5000).compareTo(c.getValue().getDiferencia()));
		assertEquals("Se dio mal un vuelto", c.getValue().getObservacion());
	}

	@Test
	void amountsMustBeWholeAndNotNegative() {
		assertThrows(BusinessLogicException.class, () -> service.cerrar(null, BigDecimal.ZERO, null, null));
		assertThrows(BusinessLogicException.class,
				() -> service.cerrar(null, gs(-1), gs(70000), null));
		assertThrows(BusinessLogicException.class,
				() -> service.cerrar(null, BigDecimal.ZERO, new BigDecimal("70000.5"), null));
		verify(dao, never()).insertCierre(any());
	}

	@Test
	void aClosedDayCannotBeClosedAgain() {
		service.cerrar(null, BigDecimal.ZERO, gs(70000), null);
		BusinessLogicException e = assertThrows(BusinessLogicException.class,
				() -> service.cerrar(null, BigDecimal.ZERO, gs(70000), null));
		assertTrue(e.getMessage().contains("ya se cerró"));
	}

	@Test
	void onlyAnAdministratorCancelsAClosing() {
		assertThrows(BusinessLogicException.class, () -> service.anular(40L, "Se contó mal"));
		verify(dao, never()).anularCierre(anyLong(), anyLong(), anyString(), anyString());
	}

	@Test
	void cancellingAClosingReopensTheDay() {
		service.cerrar(null, BigDecimal.ZERO, gs(70000), null);
		CajaCierre guardado = cierres.get(0);
		when(usuarioActual.esAdministrador()).thenReturn(true);
		when(dao.getCierre(40L)).thenReturn(guardado);
		when(dao.anularCierre(40L, 3L, "Caja Uno", "Se contó mal")).thenAnswer(i -> {
			guardado.setEstado(CajaCierre.ANULADO);
			return true;
		});

		assertThrows(BusinessLogicException.class, () -> service.anular(40L, "no"));
		CajaResumen r = service.anular(40L, "Se contó mal");

		assertNull(r.getCierre());
		assertEquals(1, r.getCierresAnulados().size());
		verify(auditoria).registrar(eq(Auditoria.ANULAR), eq(Auditoria.CAJA), eq(40L), anyString(), any(), any());
		assertNull(service.anular(99L, "No existe este"));
	}
}
