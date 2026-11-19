package com.marithe.sysfact.security;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.marithe.sysfact.dao.UsuarioDao;
import com.marithe.sysfact.exception.BusinessLogicException;
import com.marithe.sysfact.model.Usuario;

/**
 * The user of the current request, from the JWT: its subject is the e-mail and its only authority
 * is the role name.
 */
@Component
public class UsuarioActual {

	public static final String ADMINISTRADOR = "Administrador";

	@Autowired
	private UsuarioDao usuarioDao;

	/** E-mail of the logged-in user, or null outside a request. */
	public String getCorreo() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		return auth == null ? null : auth.getName();
	}

	/** Role name of the logged-in user, or null. */
	public String getRol() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null) {
			return null;
		}
		return auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).findFirst().orElse(null);
	}

	public boolean esAdministrador() {
		return ADMINISTRADOR.equals(getRol());
	}

	/** The logged-in user, read from the database. */
	public Usuario get() {
		String correo = getCorreo();
		List<Usuario> usuarios = correo == null ? List.of() : usuarioDao.findByCorreoIgnoreCase(correo);
		if (usuarios.isEmpty()) {
			throw new BusinessLogicException("No se pudo identificar al usuario de la sesión. Volvé a iniciar sesión.");
		}
		return usuarios.get(0);
	}
}
