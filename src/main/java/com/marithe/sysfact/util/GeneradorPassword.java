package com.marithe.sysfact.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GeneradorPassword {

    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = "12345";
        String encodedPassword = encoder.encode(rawPassword);

        System.out.println("Contraseña: " + rawPassword);
        System.out.println("Hash generado: " + encodedPassword);
    }
}