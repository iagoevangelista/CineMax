package com.cinemax.backend.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource; 
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.config.Customizer;
import org.springframework.http.HttpMethod;

import java.util.Arrays; 

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity

public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)

                .authorizeHttpRequests(auth -> auth
                        // 1. Permitir peticiones preflight de Angular (CORS) - ¡ESTO FALTABA!
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 2. Rutas públicas: Todo lo que empiece con /api/v1/auth/ está permitido
                        .requestMatchers("/api/v1/auth/**").permitAll()

                        // Rutas públicas: Películas (cartelera pública)
                        .requestMatchers(HttpMethod.GET, "/api/v1/movies/**").permitAll()
                        .requestMatchers("/api/v1/rooms/**").authenticated()


                        .requestMatchers("/api/v1/showtimes/**").permitAll() 
                        .requestMatchers("/api/v1/seats/**").permitAll()
                        .requestMatchers("/api/v1/venues/public").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/snacks/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/snack-categories/**").permitAll()


                        .requestMatchers( "/api/v1/genres/**").permitAll()
                        .requestMatchers( "/api/v1/classifications/**").permitAll()

                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/api/v1/auth/**", "/error").permitAll()
                        .requestMatchers("/api/v1/auth/reset-password").permitAll()
                        // 3. Rutas públicas: Swagger y documentación
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

                        .requestMatchers(HttpMethod.POST, "/api/v1/movies", "/api/v1/movies/**").hasAnyAuthority("ROLE_GERENTE_OPERACIONES", "GERENTE_DE_OPERACIONES", "ROLE_GERENTE_GENERAL", "GERENTE_GENERAL")
    .requestMatchers(HttpMethod.PUT, "/api/v1/movies", "/api/v1/movies/**").hasAnyAuthority("ROLE_GERENTE_OPERACIONES", "GERENTE_DE_OPERACIONES", "ROLE_GERENTE_GENERAL", "GERENTE_GENERAL")
    .requestMatchers(HttpMethod.DELETE, "/api/v1/movies", "/api/v1/movies/**").hasAnyAuthority("ROLE_GERENTE_OPERACIONES", "GERENTE_DE_OPERACIONES", "ROLE_GERENTE_GENERAL", "GERENTE_GENERAL")

                        // 4. Cualquier otra ruta requiere estar autenticado
                        .anyRequest().authenticated()
                        
                )

                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:4200")); 
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}