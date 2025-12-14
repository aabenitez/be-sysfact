package com.marithe.sysfact.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
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

		UsernamePasswordAuthenticationToken authentication = getAuthentication(req);
		SecurityContextHolder.getContext().setAuthentication(authentication);
		chain.doFilter(req, res);
	}

	// Reads the JWT from the Authorization header, and then uses JWT to validate
	// the token
	private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request) {
		String token = request.getHeader(SecurityConstants.HEADER_STRING);
		Usuario usuario = null;
		GrantedAuthority authority = null;

		if (token != null) {

			try {
				// parse the token.
				String user = Jwts.parser().setSigningKey(SecurityConstants.SECRET)
						.parseClaimsJws(token.replace(SecurityConstants.TOKEN_PREFIX, "")).getBody().getSubject();

				if (user != null) {
					// new arraylist means authorities

					usuario = usuarioService.findByEmail(user.toString());

					authority = new SimpleGrantedAuthority(usuario.getRol().getNombre());

					return new UsernamePasswordAuthenticationToken(usuario.getCorreo(), usuario.getContrasena(),
							Arrays.asList(authority));
				}

			} catch (ExpiredJwtException ex) {
				// request.setAttribute("expired","El token ha expirado. Vuelva a loguearse");
				request.setAttribute("expired", ex.getMessage());
				throw new CredentialsExpiredException("El token ha expirado", ex);

			}

		}

		return null;
	}
}