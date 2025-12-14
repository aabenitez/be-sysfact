package py.com.ventasjdbc.model;

import javax.validation.constraints.NotBlank;

public class Parametro extends BaseEntity {
    @NotBlank(message = "La descripcion no puede estar en blanco")
    private String descripcion;

    @NotBlank(message = "El codigo no puede estar en blanco")
    private String codigo;

    private Boolean activo;

    @NotBlank(message = "El valor no puede estar en blanco")
    private String valor;

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }
}
