package py.com.ventasjdbc.exception;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.http.HttpStatus;

public class ErrorResponse {

	private String fecha;
	private HttpStatus estado;
	private String mensaje;
	private List<String> error;

	public ErrorResponse(HttpStatus estado, String mensaje, List<String> error) {
		super();

		LocalDateTime now = LocalDateTime.now();

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

		String formatDateTime = now.format(formatter);

		this.fecha = formatDateTime;
		this.estado = estado;
		this.mensaje = mensaje;
		this.error = error;
	}

	public String getFecha() {
		return fecha;
	}

	public void setFecha(String fecha) {
		this.fecha = fecha;
	}

	public HttpStatus getEstado() {
		return estado;
	}

	public void setEstado(HttpStatus estado) {
		this.estado = estado;
	}

	public String getMensaje() {
		return mensaje;
	}

	public void setMensaje(String mensaje) {
		this.mensaje = mensaje;
	}

	public List<String> getError() {
		return error;
	}

	public void setError(List<String> error) {
		this.error = error;
	}

	@Override
	public String toString() {
		return "ErrorResponse [fecha=" + fecha + ", estado=" + estado + ", mensaje=" + mensaje + ", error=" + error
				+ "]";
	}

}
