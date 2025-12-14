package com.marithe.sysfact.dto;

import javax.validation.constraints.NotBlank;

public class CambiarPasswordDTO {
    @NotBlank(message = "La contraseña no puede estar en blanco")
    private String password;
    @NotBlank(message = "Debe confirmar la contraseña")
    private String passwordConfirm;

    public CambiarPasswordDTO() {
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
