package py.com.ventasjdbc.security;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import py.com.ventasjdbc.model.Usuario;
import py.com.ventasjdbc.service.UsuarioService;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

	@Autowired
	private UsuarioService userService;

	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		Usuario user = userService.findByEmail(email);

		if (user == null) {
			throw new UsernameNotFoundException(email);
		}

		GrantedAuthority authority = new SimpleGrantedAuthority(user.getRol().getNombre());

		return org.springframework.security.core.userdetails.User.withUsername(user.getCorreo())
				.password(user.getContrasena()).authorities(Arrays.asList(authority)).accountExpired(false)
				.accountLocked(false).credentialsExpired(false).disabled(false).build();
	}
}
