package com.marithe.sysfact.service;

/** Password recovery through a single-use link sent by e-mail. */
public interface RecuperarClaveService {

	/**
	 * Creates a recovery token for the user with that e-mail (valid for 1 hour) and e-mails the link.
	 * Does nothing visible when the e-mail is not registered, so the answer never reveals who has an account.
	 */
	void solicitar(String correo);

	/** Whether the token from the link exists and has not expired. */
	boolean esValido(String token);

	/**
	 * Sets the new password of the token's user and invalidates the token.
	 *
	 * @throws com.marithe.sysfact.exception.BusinessLogicException if the token is invalid or expired,
	 *                                                              or the passwords do not match
	 */
	void cambiar(String token, String password, String passwordConfirm);
}
