package com.marithe.sysfact.security;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import com.marithe.sysfact.model.Usuario;
import com.marithe.sysfact.service.UsuarioService;
import com.marithe.sysfact.util.SecurityConstants;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;

public class JWTAuthorizationFilter extends BasicAuthenticationFilter {

	private UsuarioService usuarioService;

	public JWTAuthorizationFilter(AuthenticationManager authManager, UsuarioService usuarioService) {
		super(authManager);
		this.usuarioService = usuarioService;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
			throws IOException, ServletException {
		String header = req.getHeader(SecurityConstants.HEADER_STRING);

		if (header == null || !header.startsWith(SecurityConstants.TOKEN_PREFIX)) {
			chain.doFilter(req, res);
			return;
		}

		UsernamePasswordAuthenticationToken authentication = getAuthentication(req);

		SecurityContextHolder.getContext().setAuthentication(authentication);
		chain.doFilter(req, res);
	}

	private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request)
			throws ExpiredJwtException, SignatureException, MalformedJwtException, UnsupportedJwtException,
			IllegalArgumentException {

		String token = request.getHeader(SecurityConstants.HEADER_STRING);

		if (token != null) {
			// Se extrae el 'subject' (email) del token JWT
			String email = io.jsonwebtoken.Jwts.parser()
					.setSigningKey(SecurityConstants.SECRET)
					.parseClaimsJws(token.replace(SecurityConstants.TOKEN_PREFIX, ""))
					.getBody()
					.getSubject();

			if (email != null) {
				// Buscamos al usuario para obtener su rol mapeado por UsuarioMapper
				Usuario usuario = usuarioService.findByEmail(email);

				if (usuario != null && usuario.getRol() != null) {
					// Creamos la autoridad basada en el nombre del rol (ej: "Administrador")
					List<SimpleGrantedAuthority> authorities = Arrays.asList(
							new SimpleGrantedAuthority(usuario.getRol().getNombre())
					);

					// Retornamos el token con el email y la lista de autoridades reales
					return new UsernamePasswordAuthenticationToken(email, null, authorities);
				}
			}
		}
		return null;
	}
}