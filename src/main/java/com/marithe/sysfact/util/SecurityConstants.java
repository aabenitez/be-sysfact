package com.marithe.sysfact.util;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.logging.Logger;

public class SecurityConstants {

	/** System property (WildFly) or environment variable that holds the JWT signing key, in Base64. */
	public static final String SECRET_PROPERTY = "sysfact.jwt.secret";
	public static final String SECRET_ENV = "SYSFACT_JWT_SECRET";

	/**
	 * Base64 key used to sign the login tokens (HS512). It is read from the system property
	 * {@value #SECRET_PROPERTY} or the environment variable {@value #SECRET_ENV}. When neither is set,
	 * a random key is generated at startup: tokens then stop being valid after a restart, which only
	 * means logging in again (they last 15 minutes anyway).
	 */
	public static final String SECRET = resolveSecret();
	public static final long EXPIRATION_TIME = 900_000; // 15 mins
	public static final String TOKEN_PREFIX = "Bearer ";
	public static final String HEADER_STRING = "Authorization";
	public static final String LOGIN_URL = "/login";

	private SecurityConstants() {
	}

	private static String resolveSecret() {
		String configured = System.getProperty(SECRET_PROPERTY);
		if (configured == null || configured.trim().isEmpty()) {
			configured = System.getenv(SECRET_ENV);
		}
		if (configured != null && !configured.trim().isEmpty()) {
			String key = configured.trim();
			if (Base64.getDecoder().decode(key).length < 64) {
				throw new IllegalStateException(SECRET_PROPERTY + " must be at least 64 bytes in Base64 "
						+ "(generate it with: openssl rand -base64 64 | tr -d '\\n')");
			}
			return key;
		}
		byte[] random = new byte[64];
		new SecureRandom().nextBytes(random);
		Logger.getLogger(SecurityConstants.class.getName()).info("No " + SECRET_PROPERTY
				+ " configured: using a random JWT key (sessions end when the server restarts).");
		return Base64.getEncoder().encodeToString(random);
	}
}
