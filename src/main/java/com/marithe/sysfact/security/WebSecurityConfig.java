package py.com.ventasjdbc.security;

import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.fasterxml.jackson.databind.ObjectMapper;

import py.com.ventasjdbc.exception.ErrorResponse;
import py.com.ventasjdbc.service.UsuarioService;
import py.com.ventasjdbc.util.SecurityConstants;

@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	private UsuarioService userService;

	@Autowired
	private UserDetailsServiceImpl userDetailsServiceImpl;

	private Logger LOGGER = Logger.getLogger(getClass().getName());

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {

		// Enable CORS and disable CSRF
		http = http.cors().and().csrf().disable();

		// Set session management to stateless
		http = http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and();

		// Set Unauthorized requests exception handler
		http = http.exceptionHandling().authenticationEntryPoint(getAuthenticationEntryPoint()).and();

		// Set Forbidden requests exception handler
		http = http.exceptionHandling().accessDeniedHandler(accessDeniedHandler()).and();

		// Set permissions on endpoints
		http.authorizeRequests()
				// Our public endpoints
				.antMatchers(HttpMethod.POST, SecurityConstants.LOGIN_URL).permitAll()
				.antMatchers(HttpMethod.POST, "/usuarios").permitAll().antMatchers(HttpMethod.POST, "/auth").permitAll()
				.antMatchers(HttpMethod.POST, "/recuperar-clave/**").permitAll()
				// Our private endpoints

				.antMatchers(HttpMethod.GET, "/paises/**").hasAnyAuthority("Cargador", "Administrador")
				.antMatchers(HttpMethod.POST, "/paises/**").hasAnyAuthority("Administrador")
				.antMatchers(HttpMethod.PUT, "/paises/**").hasAnyAuthority("Administrador")
				.antMatchers(HttpMethod.DELETE, "/paises/**").hasAnyAuthority("Administrador")

				.antMatchers(HttpMethod.GET, "/ciudades/**").hasAnyAuthority("Cargador", "Administrador")
				.antMatchers(HttpMethod.POST, "/ciudades/**").hasAnyAuthority("Administrador")
				.antMatchers(HttpMethod.PUT, "/ciudades/**").hasAnyAuthority("Administrador")
				.antMatchers(HttpMethod.DELETE, "/ciudades/**").hasAnyAuthority("Administrador")

				.antMatchers(HttpMethod.GET, "/sucursales/**").hasAnyAuthority("Cargador", "Administrador")
				.antMatchers(HttpMethod.POST, "/sucursales/**").hasAnyAuthority("Administrador")
				.antMatchers(HttpMethod.PUT, "/sucursales/**").hasAnyAuthority("Administrador")
				.antMatchers(HttpMethod.DELETE, "/sucursales/**").hasAnyAuthority("Administrador")

				.antMatchers(HttpMethod.GET, "/depositos/**").hasAnyAuthority("Cargador", "Administrador")
				.antMatchers(HttpMethod.POST, "/depositos/**").hasAnyAuthority("Administrador")
				.antMatchers(HttpMethod.PUT, "/depositos/**").hasAnyAuthority("Administrador")
				.antMatchers(HttpMethod.DELETE, "/depositos/**").hasAnyAuthority("Administrador")

				.antMatchers(HttpMethod.GET, "/sectores/**").hasAnyAuthority("Cargador", "Administrador")
				.antMatchers(HttpMethod.POST, "/sectores/**").hasAnyAuthority("Administrador")
				.antMatchers(HttpMethod.PUT, "/sectores/**").hasAnyAuthority("Administrador")
				.antMatchers(HttpMethod.DELETE, "/sectores/**").hasAnyAuthority("Administrador")

				.antMatchers(HttpMethod.GET, "/unidades/**").hasAnyAuthority("Cargador", "Administrador")
				.antMatchers(HttpMethod.POST, "/unidades/**").hasAnyAuthority("Administrador")
				.antMatchers(HttpMethod.PUT, "/unidades/**").hasAnyAuthority("Administrador")
				.antMatchers(HttpMethod.DELETE, "/unidades/**").hasAnyAuthority("Administrador")

				.antMatchers(HttpMethod.GET, "/productos/**").hasAnyAuthority("Cargador", "Administrador")
				.antMatchers(HttpMethod.POST, "/productos/**").hasAnyAuthority("Administrador")
				.antMatchers(HttpMethod.PUT, "/productos/**").hasAnyAuthority("Administrador")
				.antMatchers(HttpMethod.DELETE, "/productos/**").hasAnyAuthority("Administrador")

				.antMatchers("/roles/**").hasAnyAuthority("Administrador")

				.antMatchers(HttpMethod.GET, "/clientes/**").hasAnyAuthority("Cargador", "Administrador")
				.antMatchers(HttpMethod.POST, "/clientes/**").hasAnyAuthority("Administrador")
				.antMatchers(HttpMethod.PUT, "/clientes/**").hasAnyAuthority("Administrador")
				.antMatchers(HttpMethod.DELETE, "/clientes/**").hasAnyAuthority("Administrador")

				.antMatchers(HttpMethod.GET, "/ventas/**").hasAnyAuthority("Cargador", "Administrador")
				.antMatchers(HttpMethod.POST, "/ventas/**").hasAnyAuthority("Administrador")
				.antMatchers(HttpMethod.PUT, "/ventas/**").hasAnyAuthority("Administrador")
				.antMatchers(HttpMethod.DELETE, "/ventas/**").hasAnyAuthority("Administrador")

				.antMatchers("/reportes/**").hasAnyAuthority("Administrador")

				.anyRequest().authenticated();

		http.addFilter(new JWTAuthenticationFilter(authenticationManager(), userService));
		http.addFilter(new JWTAuthorizationFilter(authenticationManager(), userService)).sessionManagement();

	}

	@Override
	public void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(userDetailsServiceImpl).passwordEncoder(passwordEncoder());
	}

	@Bean
	CorsConfigurationSource corsConfigurationSource() {
		final CorsConfiguration configuration = new CorsConfiguration();

		configuration.setAllowedOrigins(Arrays.asList("*"));
		// configuration.setAllowedMethods(Arrays.asList("HEAD", "GET", "POST", "PUT",
		// "DELETE", "PATCH", "OPTIONS"));
		configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
		configuration.setAllowCredentials(true);
		configuration.setAllowedHeaders(Arrays.asList("*"));
		configuration.setExposedHeaders(Arrays.asList("X-Auth-Token", "Authorization", "Access-Control-Allow-Origin",
				"Access-Control-Allow-Credentials"));

		final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);

		return source;
	}

	private AuthenticationEntryPoint getAuthenticationEntryPoint() {

		return new AuthenticationEntryPoint() {

			@Override
			public void commence(HttpServletRequest request, HttpServletResponse response,
					AuthenticationException authException) throws IOException, ServletException {

				final String expired = (String) request.getAttribute("expired");
				
				if (expired != null) {
					LOGGER.severe("Bitácora de errores: " + authException.getLocalizedMessage());
					authException.printStackTrace();

					String mensaje = "El token ha expirado. Vuelva a loguearse";
					ErrorResponse body = new ErrorResponse(HttpStatus.UNAUTHORIZED, mensaje,
							Arrays.asList(authException.getMessage()));

					response.getWriter().write(new ObjectMapper().writeValueAsString(body));
					response.setStatus(401);
					response.setContentType("application/json");
				} else {
					if (authException.getClass() == InsufficientAuthenticationException.class) {
						LOGGER.severe("Bitácora de errores: " + authException.getLocalizedMessage());
						authException.printStackTrace();

						String mensaje = "Los datos enviados para la autenticación son incorrectos. ";
						ErrorResponse body = new ErrorResponse(HttpStatus.UNAUTHORIZED, mensaje,
								Arrays.asList(authException.getMessage()));

						response.getWriter().write(new ObjectMapper().writeValueAsString(body));
						response.setStatus(401);
						response.setContentType("application/json");

					} else {

						LOGGER.severe("Bitácora de errores: " + authException.getLocalizedMessage());
						authException.printStackTrace();

						String mensaje = "No tiene autorización para acceder al recurso. ";
						ErrorResponse body = new ErrorResponse(HttpStatus.UNAUTHORIZED, mensaje,
								Arrays.asList(authException.getMessage()));

						response.getWriter().write(new ObjectMapper().writeValueAsString(body));
						response.setStatus(401);
						response.setContentType("application/json");
					}
				}

			}
		};
	}

	@Bean
	public AccessDeniedHandler accessDeniedHandler() {
		return (request, response, ex) -> {
			LOGGER.severe("Bitácora de errores: " + ex.getLocalizedMessage());
			ex.printStackTrace();

			String mensaje = "No tiene permisos para acceder al recurso. ";
			ErrorResponse body = new ErrorResponse(HttpStatus.FORBIDDEN, mensaje, Arrays.asList(ex.getMessage()));

			response.getWriter().write(new ObjectMapper().writeValueAsString(body));
			response.setStatus(403);
			response.setContentType("application/json");
		};
	}
}
