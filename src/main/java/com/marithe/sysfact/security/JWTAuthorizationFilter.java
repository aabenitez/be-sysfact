package com.marithe.sysfact.security;

import io.jsonwebtoken.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import com.marithe.sysfact.model.Usuario;
import com.marithe.sysfact.service.UsuarioService;
import com.marithe.sysfact.util.SecurityConstants;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;

public class JWTAuthorizationFilter extends BasicAuthenticationFilter {

	private UsuarioService usuarioService;

	public JWTAuthorizationFilter(AuthenticationManager authManager, UsuarioService userService) {
		super(authManager);
		this.usuarioService = userService;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
			throws IOException, ServletException {

		String header = req.getHeader(SecurityConstants.HEADER_STRING);

		if (header == null || !header.startsWith(SecurityConstants.TOKEN_PREFIX)) {
			chain.doFilter(req, res);
			return;
		}

		try {
			UsernamePasswordAuthenticationToken authentication = getAuthentication(req);
			SecurityContextHolder.getContext().setAuthentication(authentication);
		} catch (ExpiredJwtException ex) {
			// Atrapamos la expiración y le pasamos el atributo al WebSecurityConfig
			req.setAttribute("expired", ex.getMessage());
		} catch (SignatureException | MalformedJwtException | UnsupportedJwtException | IllegalArgumentException ex) {
			// Atrapamos cualquier otro error (token manipulado, mal formado, etc)
			SecurityContextHolder.clearContext();
		}

		// Continuamos con la cadena de filtros.
		chain.doFilter(req, res);
	}

	// Reads the JWT from the Authorization header, and then uses JWT to validate
	// the token
	private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request)
			throws ExpiredJwtException, SignatureException, MalformedJwtException, UnsupportedJwtException, IllegalArgumentException {

		String token = request.getHeader(SecurityConstants.HEADER_STRING);

		Usuario usuario = null;
		GrantedAuthority authority = null;

		if (token != null) {
			// Al hacer el parse, si está expirado, lanzará ExpiredJwtException automáticamente hacia arriba
			String user = io.jsonwebtoken.Jwts.parser()
					.setSigningKey(SecurityConstants.SECRET)
					.parseClaimsJws(token.replace(SecurityConstants.TOKEN_PREFIX, ""))
					.getBody()
					.getSubject();

			if (user != null) {
				// Si usas roles, aquí los cargas usando tu userService
				return new UsernamePasswordAuthenticationToken(
						user, null, new java.util.ArrayList<>());
			}
			return null;
		}
		return null;
	}
}