package com.cinemax.backend.security;

import com.cinemax.backend.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class ApplicationConfig {

    private final UserAccountRepository repository;

    // 1. EL SERVICIO DE DETALLES DE USUARIO
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> repository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado en CineMax"));
    }

    // 2. EL PROVEEDOR DE AUTENTICACIÓN
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    // 3. EL GESTOR DE AUTENTICACIÓN
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // 4. EL ENCRIPTADOR DE CONTRASEÑAS
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}