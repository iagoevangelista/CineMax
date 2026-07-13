package com.cinemax.facturacion.client;

import com.cinemax.facturacion.dto.external.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "usuarios-service")
public interface UsuariosClient {

    @GetMapping("/api/users/profile")
    UserDTO getProfile();
}