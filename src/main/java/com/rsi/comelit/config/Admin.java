package com.rsi.comelit.config;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class Admin {

    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = "@Dmin123";
        String hashed = encoder.encode(rawPassword);

        System.out.println("INSERT INTO users (first_name, last_name, email, password, role) " +
                "VALUES ('admin', 'comelitRSI', 'comelitrsi@gmail.com', '" + hashed + "', 'admin');");
    }
}
