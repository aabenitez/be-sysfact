package com.marithe.sysfact.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.marithe.sysfact.dto.ApiUserDTO;
import com.marithe.sysfact.dto.UsuarioDTO;
import com.marithe.sysfact.exception.ErrorResponse;
import com.marithe.sysfact.model.Usuario;
import com.marithe.sysfact.service.UsuarioService;
import com.marithe.sysfact.util.SecurityConstants;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JWTAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

	private UsuarioService userService;

	private AuthenticationManager authenticationManager;

	private Logger LOGGER = Logger.getLogger(getClass().getName());

	public JWTAuthenticationFilter(AuthenticationManager authenticationManager, UsuarioService userService) {
		this.authenticationManager = authenticationManager;
		this.userService = userService;
	}

	@Override
	public Authentication attemptAuthentication(HttpServletRequest req, HttpServletResponse res)
			throws AuthenticationException {

		ApiUserDTO user = null;
		Usuario usuario = null;
		GrantedAuthority authority = null;
		try {

			user = new ObjectMapper().readValue(req.getInputStream(), ApiUserDTO.class);

			usuario = userService.findByEmail(user.getUsername());

			authority = new SimpleGrantedAuthority(usuario.getRol().getNombre());

			LOGGER.log(Level.FINE, "api user:", user);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(user.getUsername(),
				user.getPassword(), Arrays.asList(authority)));

	}

	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
			Authentication auth) throws IOException {
		String token = Jwts.builder().setIssuedAt(new Date()).setSubject(((User) auth.getPrincipal()).getUsername())
				.setExpiration(new Date(System.currentTimeMillis() + SecurityConstants.EXPIRATION_TIME))
				.signWith(SignatureAlgorithm.HS512, SecurityConstants.SECRET).compact();

		User user = (User) auth.getPrincipal();
		UsuarioDTO userDto = new UsuarioDTO(userService.findByEmail(user.getUsername()), token);

		JSONObject jsonObject = new JSONObject();

		try {
			JSONObject userObject = new JSONObject(new ObjectMapper().writeValueAsString(userDto));
			jsonObject.put("user", userObject);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(jsonObject.toString());
		response.getWriter().flush();

		response.addHeader(SecurityConstants.HEADER_STRING, SecurityConstants.TOKEN_PREFIX + " " + token);
	}

	@Override
	protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException failed) throws IOException, ServletException {

		if (failed.getClass() == BadCredentialsException.class) {

			String mensaje = "Error al autenticar. Credenciales de acceso incorrectas. ";
			ErrorResponse body = new ErrorResponse(HttpStatus.UNAUTHORIZED, mensaje,
					Arrays.asList(failed.getMessage()));

			response.getWriter().write(new ObjectMapper().writeValueAsString(body));
			response.setStatus(401);
			response.setContentType("application/json");

			LOGGER.severe("Bitácora de errores: " + failed.getLocalizedMessage());
			failed.printStackTrace();

		}

	}
}
