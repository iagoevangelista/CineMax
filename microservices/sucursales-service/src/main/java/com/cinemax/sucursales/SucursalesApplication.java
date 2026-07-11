package com.cinemax.sucursales;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.cinemax")
@EnableDiscoveryClient
public class SucursalesApplication {
    public static void main(String[] args) {
        SpringApplication.run(SucursalesApplication.class, args);
    }
}