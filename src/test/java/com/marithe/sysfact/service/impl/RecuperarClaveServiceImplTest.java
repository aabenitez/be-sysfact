package com.marithe.sysfact.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import com.marithe.sysfact.constants.ApplicationConstants;
import com.marithe.sysfact.exception.BusinessLogicException;
import com.marithe.sysfact.model.Parametro;
import com.marithe.sysfact.model.Usuario;
import com.marithe.sysfact.service.ParametroService;
import com.marithe.sysfact.service.UsuarioService;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RecuperarClaveServiceImplTest {

	private static final String URL = "https://sysfact.example/cambiar-contrasena?token=";

	@Mock
	private UsuarioService usuarioService;
	@Mock
	private ParametroService parametroService;
	@Mock
	private JavaMailSender mailSender;
	@InjectMocks
	private RecuperarClaveServiceImpl service;

	private Usuario ana;

	@BeforeEach
	void setUp() {
		ana = new Usuario();
		ana.setId(7L);
		ana.setNombres("Ana");
		ana.setCorreo("ana@example.com");
		when(parametroService.findByCodigo(ApplicationConstants.URL_CHANGE_PASSWORD)).thenReturn(parametro(URL));
		when(parametroService.findByCodigo(ApplicationConstants.EMAIL_USER)).thenReturn(parametro("sysfact@example.com"));
	}

	@Test
	void solicitarGuardaElHashYMandaElEnlaceConElToken() {
		when(usuarioService.findByEmail("ana@example.com")).thenReturn(ana);

		service.solicitar(" ana@example.com ");

		ArgumentCaptor<String> guardado = ArgumentCaptor.forClass(String.class);
		verify(usuarioService).setPasswordToken(eq(ana), guardado.capture());
		ArgumentCaptor<SimpleMailMessage> correo = ArgumentCaptor.forClass(SimpleMailMessage.class);
		verify(mailSender, timeout(2000)).send(correo.capture());

		String texto = correo.getValue().getText();
		int inicio = texto.indexOf(URL) + URL.length();
		String token = texto.substring(inicio, texto.indexOf('\n', inicio));
		assertEquals(43, token.length());
		assertEquals(RecuperarClaveServiceImpl.hash(token), guardado.getValue());
		assertNotEquals(token, guardado.getValue());
		assertEquals("ana@example.com", correo.getValue().getTo()[0]);
		assertEquals("sysfact@example.com", correo.getValue().getFrom());
	}

	@Test
	void solicitarConCorreoDesconocidoNoHaceNada() {
		service.solicitar("nadie@example.com");

		verify(usuarioService, never()).setPasswordToken(any(), anyString());
		verify(mailSender, never()).send(any(SimpleMailMessage.class));
	}

	@Test
	void cambiarBuscaPorHashYCambiaLaContrasena() {
		when(usuarioService.findByPasswordToken(RecuperarClaveServiceImpl.hash("abc"))).thenReturn(ana);

		service.cambiar("abc", "nueva-clave", "nueva-clave");

		verify(usuarioService).changePassoword("ana@example.com", "nueva-clave");
	}

	@Test
	void cambiarConTokenVencidoFalla() {
		BusinessLogicException e = assertThrows(BusinessLogicException.class,
				() -> service.cambiar("vencido", "nueva-clave", "nueva-clave"));
		assertEquals(RecuperarClaveServiceImpl.TOKEN_INVALIDO, e.getMessage());
		verify(usuarioService, never()).changePassoword(anyString(), anyString());
	}

	@Test
	void cambiarConContrasenasDistintasFalla() {
		BusinessLogicException e = assertThrows(BusinessLogicException.class,
				() -> service.cambiar("abc", "nueva-clave", "otra-clave"));
		assertEquals(RecuperarClaveServiceImpl.NO_COINCIDEN, e.getMessage());
	}

	@Test
	void esValido() {
		when(usuarioService.findByPasswordToken(RecuperarClaveServiceImpl.hash("abc"))).thenReturn(ana);
		assertTrue(service.esValido("abc"));
		assertFalse(service.esValido("otro"));
		assertFalse(service.esValido(" "));
		assertFalse(service.esValido(null));
	}

	private static Parametro parametro(String valor) {
		Parametro p = new Parametro();
		p.setValor(valor);
		return p;
	}
}
