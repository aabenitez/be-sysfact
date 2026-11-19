package com.marithe.sysfact.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.marithe.sysfact.dao.UsuarioDao;
import com.marithe.sysfact.exception.BusinessLogicException;
import com.marithe.sysfact.model.Rol;
import com.marithe.sysfact.model.Usuario;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UsuarioServiceImplTest {

	private static final long ADMIN_ID = 1L;
	private static final long CARGADOR_ID = 2L;

	@Mock
	private UsuarioDao dao;
	@Mock
	private PasswordEncoder encoder;
	@InjectMocks
	private UsuarioServiceImpl service;

	@BeforeEach
	void setUp() {
		when(dao.findRolNombre(ADMIN_ID)).thenReturn("Administrador");
		when(dao.findRolNombre(CARGADOR_ID)).thenReturn("Cargador");
		when(dao.findByCorreoIgnoreCase(anyString())).thenReturn(Collections.emptyList());
		when(encoder.encode(anyString())).thenAnswer(i -> "hash:" + i.getArgument(0));
		when(dao.insert(any())).thenAnswer(i -> i.getArgument(0));
		// Logged in as the admin ana@example.com
		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken("ana@example.com", null, Collections.emptyList()));
	}

	@AfterEach
	void tearDown() {
		SecurityContextHolder.clearContext();
	}

	private static Usuario usuario(Long id, String correo, long rolId, String rolNombre, String contrasena) {
		Usuario u = new Usuario();
		u.setId(id);
		u.setNombres(" Juan ");
		u.setApellidos(" Pérez ");
		u.setCorreo(correo);
		Rol rol = new Rol();
		rol.setId(rolId);
		rol.setNombre(rolNombre);
		u.setRol(rol);
		u.setContrasena(contrasena);
		return u;
	}

	@Test
	void insertHashesPasswordTrimsAndHidesIt() {
		Usuario creado = service.insert(usuario(null, " juan@example.com ", CARGADOR_ID, null, "secreta123"));
		verify(dao).insert(any());
		assertEquals("Juan", creado.getNombres());
		assertEquals("juan@example.com", creado.getCorreo());
		assertNull(creado.getContrasena());
	}

	@Test
	void insertRequiresPassword() {
		assertThrows(BusinessLogicException.class,
				() -> service.insert(usuario(null, "juan@example.com", CARGADOR_ID, null, "  ")));
		assertThrows(BusinessLogicException.class,
				() -> service.insert(usuario(null, "juan@example.com", CARGADOR_ID, null, "corta")));
		verify(dao, never()).insert(any());
	}

	@Test
	void insertRejectsDuplicateEmailIgnoringCase() {
		when(dao.findByCorreoIgnoreCase("Juan@Example.com")).thenReturn(Collections.singletonList(
				usuario(7L, "juan@example.com", CARGADOR_ID, "Cargador", null)));
		assertThrows(BusinessLogicException.class,
				() -> service.insert(usuario(null, "Juan@Example.com", CARGADOR_ID, null, "secreta123")));
	}

	@Test
	void insertRejectsUnknownRole() {
		assertThrows(BusinessLogicException.class,
				() -> service.insert(usuario(null, "juan@example.com", 99L, null, "secreta123")));
	}

	@Test
	void updateWithoutPasswordKeepsIt() {
		when(dao.findById(7L)).thenReturn(usuario(7L, "juan@example.com", CARGADOR_ID, "Cargador", null));
		service.update(usuario(7L, "juan@example.com", CARGADOR_ID, null, ""));
		verify(dao).update(any());
		verify(dao, never()).updatePassword(anyLong(), anyString());
	}

	@Test
	void updateWithPasswordChangesIt() {
		when(dao.findById(7L)).thenReturn(usuario(7L, "juan@example.com", CARGADOR_ID, "Cargador", null));
		service.update(usuario(7L, "juan@example.com", CARGADOR_ID, null, "nueva1234"));
		verify(dao).updatePassword(7L, "hash:nueva1234");
	}

	@Test
	void adminCannotRemoveOwnAdminRole() {
		when(dao.findById(1L)).thenReturn(usuario(1L, "ana@example.com", ADMIN_ID, "Administrador", null));
		when(dao.countByRolNombre("Administrador")).thenReturn(2L);
		assertThrows(BusinessLogicException.class,
				() -> service.update(usuario(1L, "ana@example.com", CARGADOR_ID, null, null)));
		verify(dao, never()).update(any());
	}

	@Test
	void lastAdminCannotBeDemoted() {
		when(dao.findById(5L)).thenReturn(usuario(5L, "otro@example.com", ADMIN_ID, "Administrador", null));
		when(dao.countByRolNombre("Administrador")).thenReturn(1L);
		assertThrows(BusinessLogicException.class,
				() -> service.update(usuario(5L, "otro@example.com", CARGADOR_ID, null, null)));
	}

	@Test
	void cannotDeleteSelf() {
		when(dao.findById(1L)).thenReturn(usuario(1L, "ANA@example.com", ADMIN_ID, "Administrador", null));
		when(dao.countByRolNombre("Administrador")).thenReturn(3L);
		assertThrows(BusinessLogicException.class, () -> service.delete(1L));
		verify(dao, never()).delete(anyLong());
	}

	@Test
	void cannotDeleteUserWithSales() {
		when(dao.findById(7L)).thenReturn(usuario(7L, "juan@example.com", CARGADOR_ID, "Cargador", null));
		when(dao.countVentas(7L)).thenReturn(3L);
		BusinessLogicException e = assertThrows(BusinessLogicException.class, () -> service.delete(7L));
		assertEquals("No se puede eliminar el usuario porque registró 3 ventas.", e.getMessage());
	}

	@Test
	void deletesOtherUserWithoutSales() {
		when(dao.findById(7L)).thenReturn(usuario(7L, "juan@example.com", CARGADOR_ID, "Cargador", null));
		service.delete(7L);
		verify(dao).delete(7L);
	}
}
