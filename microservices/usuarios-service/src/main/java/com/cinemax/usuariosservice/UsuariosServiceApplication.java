package com.cinemax.usuariosservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableFeignClients
@ComponentScan(basePackages = {"com.cinemax.usuariosservice", "com.cinemax.common"})
public class UsuariosServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UsuariosServiceApplication.class, args);
    }
}