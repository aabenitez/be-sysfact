package com.marithe.sysfact.dao;

import com.marithe.sysfact.model.Usuario;

import java.util.List;

public interface UsuarioDao {

	List<Usuario> getAll(Usuario obj);

	Usuario findById(Long id);

	Usuario findByEmail(String email);

	Usuario validate(String email, String password);

	Usuario insert(Usuario obj);

	Usuario update(Usuario obj);

	void changePassoword(String correo, String newPassword);

	void delete(Long id);

	void setPasswordToken(Usuario obj, String token);

	Usuario findByPasswordToken(String token);
}
