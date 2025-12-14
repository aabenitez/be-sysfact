package py.com.ventasjdbc.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

public class RecuperarPasswordDTO {
    @NotBlank(message = "El correo no puede estar en blanco")
    @Email(message = "Formato inválido para correo electrónico")
    private String correo;

    public RecuperarPasswordDTO() {
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }
}
