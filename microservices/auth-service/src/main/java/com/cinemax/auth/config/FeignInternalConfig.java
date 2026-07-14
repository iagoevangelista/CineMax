package com.cinemax.auth.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignInternalConfig {

    @Bean
    public RequestInterceptor internalGatewayHeaderInterceptor() {
        return requestTemplate -> requestTemplate.header("X-Gateway-Request", "true");
    }
}