package py.com.ventasjdbc.util;

public class SecurityConstants {
	public static final String SECRET = "V3nTaz!!1";
	public static final long EXPIRATION_TIME = 900_000; // 15 mins
	public static final String TOKEN_PREFIX = "Bearer ";
	public static final String HEADER_STRING = "Authorization";
	public static final String LOGIN_URL = "/login";
}
