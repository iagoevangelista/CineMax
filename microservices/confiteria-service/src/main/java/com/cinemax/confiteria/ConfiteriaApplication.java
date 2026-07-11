package com.cinemax.confiteria;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(scanBasePackages = {"com.cinemax.confiteria", "com.cinemax.common"})
@ComponentScan(basePackages = "com.cinemax")
@EnableDiscoveryClient
public class ConfiteriaApplication {
    public static void main(String[] args) {
        SpringApplication.run(ConfiteriaApplication.class, args);
    }
}