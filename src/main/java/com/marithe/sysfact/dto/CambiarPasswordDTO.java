package com.marithe.sysfact.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/** Body for POST /recuperar-clave/cambiar: the token from the e-mail link plus the new password. */
public class CambiarPasswordDTO {
    @NotBlank(message = "Falta el código del enlace de recuperación")
    private String token;
    @NotBlank(message = "La contraseña no puede estar en blanco")
    @Size(min = 8, max = 100, message = "La contraseña debe tener entre 8 y 100 caracteres")
    private String password;
    @NotBlank(message = "Debe confirmar la contraseña")
    private String passwordConfirm;

    public CambiarPasswordDTO() {
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPasswordConfirm() {
        return passwordConfirm;
    }

    public void setPasswordConfirm(String passwordConfirm) {
        this.passwordConfirm = passwordConfirm;
    }
}
