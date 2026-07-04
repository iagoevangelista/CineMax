package com.cinemax.auth.client;
import com.cinemax.auth.dto.ValidateCredentialsRequestDTO;
import com.cinemax.auth.dto.ValidateCredentialsResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

// "usuarios-service" es el nombre del microservicio cuando lo registremos en Eureka - Feign lo resuelve solo, igual que hace el Gateway con lb://usuarios-service.

@FeignClient(name = "usuarios-service")
public interface UsuariosClient {

    @PostMapping("/internal/users/validate-credentials")
    ValidateCredentialsResponseDTO validateCredentials(@RequestBody ValidateCredentialsRequestDTO request);
}