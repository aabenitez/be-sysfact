package py.com.ventasjdbc.controller;

import java.util.Date;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.annotation.RequestScope;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import py.com.ventasjdbc.dto.UsuarioDTO;
import py.com.ventasjdbc.model.Usuario;
import py.com.ventasjdbc.service.UsuarioService;
import py.com.ventasjdbc.util.SecurityConstants;

@RestController
@RequestScope
@RequestMapping("/auth")
public class AuthController {
	
	@Autowired
	private AuthenticationManager authenticationManager;
	
	@Autowired
	private UsuarioService userService;


	private Logger LOGGER = Logger.getLogger(getClass().getName());
	
	@PostMapping("/login")
	public ResponseEntity<UsuarioDTO> login(@RequestParam String username, @RequestParam String password) {
		authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
		
		Usuario user = userService.findByEmail(username);
		
		String token = Jwts.builder().setIssuedAt(new Date()).setSubject(username)
				.setExpiration(new Date(System.currentTimeMillis() + SecurityConstants.EXPIRATION_TIME))
				.signWith(SignatureAlgorithm.HS512, SecurityConstants.SECRET).compact();
		
		UsuarioDTO userDto = new UsuarioDTO(userService.findByEmail(user.getCorreo()), token);
		
		return ResponseEntity.ok(userDto);
	}
}
