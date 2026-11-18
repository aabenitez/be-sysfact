package com.marithe.sysfact.service.impl;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.marithe.sysfact.constants.ApplicationConstants;
import com.marithe.sysfact.exception.BusinessLogicException;
import com.marithe.sysfact.model.Usuario;
import com.marithe.sysfact.service.ParametroService;
import com.marithe.sysfact.service.RecuperarClaveService;
import com.marithe.sysfact.service.UsuarioService;

@Service
public class RecuperarClaveServiceImpl implements RecuperarClaveService {

	static final String TOKEN_INVALIDO = "El enlace para cambiar la contraseña no es válido o ya venció. Pedí uno nuevo.";
	static final String NO_COINCIDEN = "Las contraseñas no coinciden";

	private static final Logger LOGGER = Logger.getLogger(RecuperarClaveServiceImpl.class.getName());
	private static final SecureRandom RANDOM = new SecureRandom();

	/** Sends the e-mails outside the request, so the response time does not reveal whether the user exists. */
	private final ExecutorService envios = Executors.newSingleThreadExecutor(r -> {
		Thread t = new Thread(r, "sysfact-recuperar-clave");
		t.setDaemon(true);
		return t;
	});

	@Autowired
	private UsuarioService usuarioService;

	@Autowired
	private ParametroService parametroService;

	@Autowired
	private JavaMailSender mailSender;

	@Override
	public void solicitar(String correo) {
		Usuario usuario = usuarioService.findByEmail(correo.trim());
		if (usuario == null) {
			return;
		}
		String token = nuevoToken();
		// The database only keeps the hash: whoever reads the table cannot use the link.
		usuarioService.setPasswordToken(usuario, hash(token));

		String enlace = parametroService.findByCodigo(ApplicationConstants.URL_CHANGE_PASSWORD).getValor() + token;
		String remitente = parametroService.findByCodigo(ApplicationConstants.EMAIL_USER).getValor();

		SimpleMailMessage mensaje = new SimpleMailMessage();
		mensaje.setFrom(remitente);
		mensaje.setTo(usuario.getCorreo());
		mensaje.setSubject("Sysfact: cambiar tu contraseña");
		mensaje.setText("Hola " + usuario.getNombres() + ",\n\n"
				+ "Pediste cambiar tu contraseña de Sysfact. Entrá a este enlace para elegir una nueva:\n\n"
				+ enlace + "\n\n"
				+ "El enlace vale por 1 hora y se puede usar una sola vez.\n"
				+ "Si no lo pediste vos, ignorá este correo: tu contraseña sigue siendo la misma.\n");

		envios.submit(() -> {
			try {
				mailSender.send(mensaje);
				LOGGER.log(Level.INFO, "Recovery e-mail sent to user id {0}", usuario.getId());
			} catch (RuntimeException e) {
				LOGGER.log(Level.SEVERE, "Could not send the recovery e-mail to user id " + usuario.getId(), e);
			}
		});
	}

	@Override
	public boolean esValido(String token) {
		return token != null && !token.trim().isEmpty() && usuarioService.findByPasswordToken(hash(token.trim())) != null;
	}

	@Override
	public void cambiar(String token, String password, String passwordConfirm) {
		if (!password.equals(passwordConfirm)) {
			throw new BusinessLogicException(NO_COINCIDEN);
		}
		Usuario usuario = usuarioService.findByPasswordToken(hash(token.trim()));
		if (usuario == null) {
			throw new BusinessLogicException(TOKEN_INVALIDO);
		}
		// Also clears the token, so the link cannot be used twice.
		usuarioService.changePassoword(usuario.getCorreo(), password);
	}

	@PreDestroy
	void cerrar() {
		envios.shutdown();
	}

	/** 32 random bytes, URL-safe Base64 (43 characters). */
	static String nuevoToken() {
		byte[] bytes = new byte[32];
		RANDOM.nextBytes(bytes);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}

	/** SHA-256 in hexadecimal. */
	static String hash(String token) {
		try {
			byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
			StringBuilder hex = new StringBuilder(digest.length * 2);
			for (byte b : digest) {
				hex.append(String.format("%02x", b));
			}
			return hex.toString();
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException(e);
		}
	}
}
