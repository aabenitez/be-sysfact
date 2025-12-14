package com.marithe.sysfact.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.marithe.sysfact.dao.UsuarioDao;
import com.marithe.sysfact.model.Usuario;
import com.marithe.sysfact.service.UsuarioService;

import java.util.List;

@Service
@Transactional
public class UsuarioServiceImpl implements UsuarioService {

    @Autowired
    private UsuarioDao userDao;

    @Autowired
    private PasswordEncoder bCryptPasswordEncoder;

    @Override
    public List<Usuario> getAll(Usuario user) {
        return userDao.getAll(user);
    }

    @Override
    public Usuario findById(Long id) {
        return userDao.findById(id);
    }

    @Override
    public Usuario findByEmail(String email) {
        return userDao.findByEmail(email);
    }

    @Override
    public Usuario validate(String email, String password) {
        return userDao.validate(email, password);
    }

    @Override
    public Usuario insert(Usuario user) {
        user.setContrasena(bCryptPasswordEncoder.encode(user.getContrasena()));
        return userDao.insert(user);
    }

    @Override
    public void delete(Long id) {
        userDao.delete(id);
    }

    @Override
    public Usuario update(Usuario obj) {
        // TODO Auto-generated method stub
        return userDao.update(obj);
    }

    @Override
    public void changePassoword(String correo, String newPassword) {    	
        userDao.changePassoword(correo, bCryptPasswordEncoder.encode(newPassword));
    }

    @Override
    public void setPasswordToken(Usuario obj, String token) {
        userDao.setPasswordToken(obj, token);
    }

    @Override
    public Usuario findByPasswordToken(String token) {
        return userDao.findByPasswordToken(token);
    }
}
