package com.web.pfc.SpringPfc;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GerarHash {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        String senhaPura = "admin123";
        String hashDoBanco = "$2a$10$HDU2r8urkk3PiXs7H2QhY.ltMXJOzH6w5.Cjh3MzpWTmGC3EoGoPm";

        boolean bateu = encoder.matches(senhaPura, hashDoBanco);

        System.out.println("Senha pura: " + senhaPura);
        System.out.println("Hash testado: " + hashDoBanco);
        System.out.println("Bate? " + bateu);
    }
}