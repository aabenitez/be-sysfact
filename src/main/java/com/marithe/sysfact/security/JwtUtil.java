package com.marithe.sysfact.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import com.marithe.sysfact.model.Rol;
import com.marithe.sysfact.model.Usuario;
import com.marithe.sysfact.util.SecurityConstants;

import java.util.Date;

public class JwtUtil {

    /**
     * Tries to parse specified String as a JWT token. If successful, returns User
     * object with username, id and role prefilled (extracted from token). If
     * unsuccessful (token is invalid or not containing all required user
     * properties), simply returns null.
     *
     * @param token the JWT token to parse
     * @return the User object extracted from specified token or null if a token is
     * invalid.
     */
    public Usuario parseToken(String token) {
        try {
            Claims body = Jwts.parser().setSigningKey(SecurityConstants.SECRET).parseClaimsJws(token).getBody();
            Usuario user = new Usuario();
            user.setCorreo(body.getSubject());
            Rol role = new Rol();
            role.setNombre((String) body.get("rol"));
            user.setRol(role);
            return user;
        } catch (JwtException | ClassCastException e) {
            return null;
        }
    }

    /**
     * Generates a JWT token containing username as subject, and userId and role as
     * additional claims. These properties are taken from the specified User object.
     * Tokens validity is infinite.
     *
     * @param the user for which the token will be generated
     * @return the JWT token
     */
    public String generateToken(Usuario user) {
        Claims claims = Jwts.claims().setSubject(user.getCorreo());
        claims.put("userId", user.getId() + "");
        claims.put("role", user.getRol());

        return Jwts.builder().setClaims(claims)
                .setExpiration(new Date(System.currentTimeMillis() + SecurityConstants.EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, SecurityConstants.SECRET)
                .compact();
    }
}
