package com.cinemax.cartelera;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class CarteleraApplication {
    public static void main(String[] args) {
        SpringApplication.run(CarteleraApplication.class, args);
    }
}