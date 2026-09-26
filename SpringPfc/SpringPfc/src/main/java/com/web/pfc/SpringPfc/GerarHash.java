package com.web.pfc.SpringPfc;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GerarHash {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String senhaPura = "02059798"; // troque pela senha que você quiser usar
        String hash = encoder.encode(senhaPura);
        System.out.println(hash);
    }
}