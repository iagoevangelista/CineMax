package com.cinemax.backend.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.Customizer;


@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. Desactivamos CSRF (No lo necesitamos porque usamos Tokens, no Cookies)
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)

                // 2. Definimos las reglas de las rutas (Endpoints)
                .authorizeHttpRequests(auth -> auth
                        // Rutas públicas: Todo lo que empiece con /api/v1/auth/ está permitido sin token
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        // Rutas públicas: Swagger y documentación
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        // Cualquier otra ruta requiere estar autenticado
                        .anyRequest().authenticated()
                )

                // 3. Gestión de Sesiones: STATELESS (Sin estado)
                // Esto significa que Spring no guardará la sesión en memoria.
                // Cada petición debe traer su propio JWT para ser validada.
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 4. Proveedor de autenticación (el que creamos en ApplicationConfig)
                .authenticationProvider(authenticationProvider)

                // 5. Agregamos nuestro filtro ANTES del filtro estándar de Spring
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}