package com.marithe.sysfact.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import com.marithe.sysfact.exception.BusinessLogicException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.marithe.sysfact.dao.UsuarioDao;
import com.marithe.sysfact.model.Usuario;
import com.marithe.sysfact.service.UsuarioService;

import java.util.List;

@Service
@Transactional
public class UsuarioServiceImpl implements UsuarioService {

    private static final String ADMIN = "Administrador";
    private static final int MIN_CONTRASENA = 8;

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
        normalizar(user);
        validarRol(user);
        validarCorreoUnico(user);
        if (!StringUtils.hasText(user.getContrasena())) {
            throw new BusinessLogicException("Ingresá una contraseña para el usuario nuevo.");
        }
        validarContrasena(user.getContrasena());
        user.setContrasena(bCryptPasswordEncoder.encode(user.getContrasena()));
        Usuario created = userDao.insert(user);
        created.setContrasena(null);
        return created;
    }

    @Override
    public void delete(Long id) {
        Usuario actual = userDao.findById(id);
        if (actual == null) {
            return;
        }
        if (esUsuarioActual(actual)) {
            throw new BusinessLogicException("No podés eliminar tu propio usuario.");
        }
        if (ADMIN.equals(actual.getRol().getNombre()) && userDao.countByRolNombre(ADMIN) <= 1) {
            throw new BusinessLogicException("No se puede eliminar al único Administrador.");
        }
        long ventas = userDao.countVentas(id);
        if (ventas > 0) {
            throw new BusinessLogicException("No se puede eliminar el usuario porque registró "
                    + ventas + (ventas == 1 ? " venta." : " ventas."));
        }
        userDao.delete(id);
    }

    @Override
    public Usuario update(Usuario obj) {
        Usuario actual = userDao.findById(obj.getId());
        normalizar(obj);
        String rolNuevo = validarRol(obj);
        validarCorreoUnico(obj);

        // Someone must always be able to manage users.
        boolean eraAdmin = actual != null && ADMIN.equals(actual.getRol().getNombre());
        if (eraAdmin && !ADMIN.equals(rolNuevo)) {
            if (esUsuarioActual(actual)) {
                throw new BusinessLogicException("No podés quitarte el rol de Administrador.");
            }
            if (userDao.countByRolNombre(ADMIN) <= 1) {
                throw new BusinessLogicException("Tiene que quedar al menos un Administrador.");
            }
        }

        boolean cambiaContrasena = StringUtils.hasText(obj.getContrasena());
        if (cambiaContrasena) {
            validarContrasena(obj.getContrasena());
        }
        userDao.update(obj);
        if (cambiaContrasena) {
            userDao.updatePassword(obj.getId(), bCryptPasswordEncoder.encode(obj.getContrasena()));
        }
        obj.setContrasena(null);
        obj.getRol().setNombre(rolNuevo);
        return obj;
    }

    /** The e-mail is the login, so two users cannot share it (not even with different case). */
    private void validarCorreoUnico(Usuario obj) {
        for (Usuario otro : userDao.findByCorreoIgnoreCase(obj.getCorreo())) {
            if (obj.getId() == null || !otro.getId().equals(obj.getId())) {
                throw new BusinessLogicException("Ya existe un usuario con el correo " + obj.getCorreo()
                        + ": " + otro.getNombres().trim() + " " + otro.getApellidos().trim() + ".");
            }
        }
    }

    /** Returns the role name; fails if the role does not exist. */
    private String validarRol(Usuario obj) {
        String nombre = userDao.findRolNombre(obj.getRol().getId());
        if (nombre == null) {
            throw new BusinessLogicException("El rol elegido no existe.");
        }
        return nombre;
    }

    private static void validarContrasena(String contrasena) {
        if (contrasena.length() < MIN_CONTRASENA || contrasena.length() > 100) {
            throw new BusinessLogicException("La contraseña debe tener entre " + MIN_CONTRASENA
                    + " y 100 caracteres.");
        }
    }

    private static boolean esUsuarioActual(Usuario usuario) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && usuario.getCorreo() != null
                && usuario.getCorreo().trim().equalsIgnoreCase(auth.getName());
    }

    /** Trims the text fields. The e-mail keeps its case, because the login compares it as typed. */
    private static void normalizar(Usuario obj) {
        obj.setNombres(obj.getNombres().trim());
        obj.setApellidos(obj.getApellidos().trim());
        obj.setCorreo(obj.getCorreo().trim());
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
