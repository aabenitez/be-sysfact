package com.marithe.sysfact.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

@ControllerAdvice
@RestController
public class CentralErrorResponse extends ResponseEntityExceptionHandler {

	private final Logger logger = Logger.getLogger(getClass().getName());
	// https://devwithus.com/exception-handling-for-rest-api-with-spring-boot/

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleAll(Exception ex, WebRequest request) {

		String error = "Se ha producido un error en el servidor. : " + ex.getStackTrace().toString();

		ErrorResponse body = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getLocalizedMessage(),
				Arrays.asList(error));

		logger.severe("Bitácora de errores: " + ex.getLocalizedMessage());
		ex.printStackTrace();

		return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler(BusinessLogicException.class)
	public ResponseEntity<ErrorResponse> bussinesLogicException(BusinessLogicException ex, WebRequest request) {

		String mensaje = "Se produjo un error al violar las reglas de negocio de la aplicación.";

		ErrorResponse body = new ErrorResponse(HttpStatus.CONFLICT, mensaje, Arrays.asList(ex.getMessage()));

		logger.severe("Bitácora de errores: " + ex.getLocalizedMessage());
		ex.printStackTrace();

		return new ResponseEntity<>(body, HttpStatus.CONFLICT);
	}

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {

		List<String> errors = new ArrayList<String>();

		for (FieldError error : ex.getBindingResult().getFieldErrors()) {
			errors.add(error.getDefaultMessage());
		}

		String mensaje = "Se produjo un error al violar las restriciones de los datos de la petición. ";

		ErrorResponse body = new ErrorResponse(HttpStatus.BAD_REQUEST, mensaje, errors);

		logger.severe("Bitácora de errores: " + ex.getLocalizedMessage());
		ex.printStackTrace();

		return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);

	}

	@ExceptionHandler(DuplicateKeyException.class)
	public ResponseEntity<ErrorResponse> duplicateKeyError(DuplicateKeyException ex, WebRequest request) {

		String mensaje = "Se produjo un error debido a que se intenta guardar un valor duplicado. ";

		ErrorResponse body = new ErrorResponse(HttpStatus.CONFLICT, mensaje, Arrays.asList(ex.getLocalizedMessage()));

		logger.severe("Bitácora de errores: " + ex.getLocalizedMessage());
		ex.printStackTrace();

		return new ResponseEntity<>(body, HttpStatus.CONFLICT);
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ErrorResponse> dataIntegrityViolationException(DataIntegrityViolationException ex,
			WebRequest request) {

		String mensaje = "Se produjo un error debido que está violando integridad referencial en la base de datos. ";

		ErrorResponse body = new ErrorResponse(HttpStatus.CONFLICT, mensaje, Arrays.asList(ex.getLocalizedMessage()));

		logger.severe("Bitácora de errores: " + ex.getLocalizedMessage());
		ex.printStackTrace();

		return new ResponseEntity<>(body, HttpStatus.CONFLICT);
	}

	@Override
	protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {

		String mensaje = "El cuerpo de la petición es incorrecta. Json con formato inválido. ";

		ErrorResponse body = new ErrorResponse(HttpStatus.BAD_REQUEST, mensaje,
				Arrays.asList(ex.getLocalizedMessage()));

		logger.severe("Bitácora de errores: " + ex.getLocalizedMessage());
		ex.printStackTrace();

		return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ErrorResponse> methodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex,
			WebRequest request) {

		String mensaje = "Los párametros de la petición son incorrectos. Tipos de datos inválidos. ";

		ErrorResponse body = new ErrorResponse(HttpStatus.BAD_REQUEST, mensaje,
				Arrays.asList(ex.getLocalizedMessage()));

		logger.severe("Bitácora de errores: " + ex.getLocalizedMessage());
		ex.printStackTrace();

		return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
	}

	@Override
	protected ResponseEntity<Object> handleMissingServletRequestParameter(MissingServletRequestParameterException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {
		String mensaje = "El controlador no recibió el parámetro requerido. ";

		ErrorResponse body = new ErrorResponse(HttpStatus.BAD_REQUEST, mensaje,
				Arrays.asList(ex.getLocalizedMessage()));

		logger.severe("Bitácora de errores: " + ex.getLocalizedMessage());
		ex.printStackTrace();

		return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
	}

	@Override
	protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {
		List<String> details = new ArrayList<String>();
		StringBuilder builder = new StringBuilder();
		builder.append(ex.getContentType());
		builder.append("Tipo de archivo no soportado. Los tipos correctos son:  ");
		ex.getSupportedMediaTypes().forEach(t -> builder.append(t).append(", "));

		details.add(builder.toString());

		String mensaje = "Tipo de archivo no soportado. ";

		ErrorResponse body = new ErrorResponse(HttpStatus.UNSUPPORTED_MEDIA_TYPE, mensaje, details);

		logger.severe("Bitácora de errores: " + ex.getLocalizedMessage());
		ex.printStackTrace();

		return new ResponseEntity<>(body, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
	}

	@Override
	protected ResponseEntity<Object> handleNoHandlerFoundException(NoHandlerFoundException ex, HttpHeaders headers,
			HttpStatus status, WebRequest request) {

		String mensaje = "No existen métodos específicos para la petición. ";

		String error = "No se encontró métodos para: " + ex.getHttpMethod() + " " + ex.getRequestURL();

		ErrorResponse body = new ErrorResponse(HttpStatus.NOT_FOUND, mensaje, Arrays.asList(error));

		logger.severe("Bitácora de errores: " + ex.getLocalizedMessage());
		ex.printStackTrace();

		return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler({ EmptyResultDataAccessException.class })
	public ResponseEntity<Object> handleNoResult(EmptyResultDataAccessException ex, WebRequest request) {

		String mensaje = "Acceso a datos con resultado vacío. ";
		ErrorResponse body = new ErrorResponse(HttpStatus.NO_CONTENT, mensaje, Arrays.asList(ex.getLocalizedMessage()));
		logger.severe("Bitácora de errores: " + ex.getLocalizedMessage());
		ex.printStackTrace();

		return new ResponseEntity<>(body, HttpStatus.NO_CONTENT);
	}

	@Override
	protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {
		String mensaje = "No se permite el método para la petición. ";

		ErrorResponse body = new ErrorResponse(HttpStatus.METHOD_NOT_ALLOWED, mensaje,
				Arrays.asList(ex.getLocalizedMessage()));

		logger.severe("Bitácora de errores: " + ex.getLocalizedMessage());
		ex.printStackTrace();

		return new ResponseEntity<>(body, HttpStatus.METHOD_NOT_ALLOWED);
	}

	/*@ExceptionHandler({ BadCredentialsException.class })
	public ResponseEntity<Object> handleAuthenticationException(BadCredentialsException ex, WebRequest request) {

		String mensaje = "Error al autenticar. ";
		ErrorResponse body = new ErrorResponse(HttpStatus.UNAUTHORIZED, mensaje,
				Arrays.asList(ex.getLocalizedMessage()));
		logger.severe("Bitácora de errores: " + ex.getLocalizedMessage());
		ex.printStackTrace();

		return new ResponseEntity<>(body, HttpStatus.UNAUTHORIZED);
	}*/

}
