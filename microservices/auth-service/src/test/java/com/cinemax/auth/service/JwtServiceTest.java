package com.cinemax.auth.service;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        // Como este es un test unitario puro
        ReflectionTestUtils.setField(jwtService, "secret",
                "586E3272357538782F413F4428472B4B6250655368566B597033733676397924");
        ReflectionTestUtils.setField(jwtService, "expirationMillis", 3600000L);
    }

    @Test
    void generateToken_creaUnTokenNoVacio() {
        String token = jwtService.generateToken(
                "gerente@cinemax.com", "GERENTE_DE_OPERACIONES", "Juan",
                2, List.of("VIEW_DASHBOARD", "MANAGE_MOVIES"));

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void generateToken_yLuegoValidarlo_esValido() {
        String token = jwtService.generateToken(
                "gerente@cinemax.com", "GERENTE_DE_OPERACIONES", "Juan",
                2, List.of("VIEW_DASHBOARD"));

        assertTrue(jwtService.isTokenValid(token));
    }

    @Test
    void extractEmail_devuelveElEmailCorrecto() {
        String token = jwtService.generateToken(
                "gerente@cinemax.com", "GERENTE_DE_OPERACIONES", "Juan",
                2, List.of("VIEW_DASHBOARD"));

        assertEquals("gerente@cinemax.com", jwtService.extractEmail(token));
    }

    @Test
    void extractAllClaims_traeRolYPermisosCorrectos() {
        String token = jwtService.generateToken(
                "gerente@cinemax.com", "GERENTE_DE_OPERACIONES", "Juan",
                2, List.of("VIEW_DASHBOARD", "MANAGE_MOVIES"));

        Claims claims = jwtService.extractAllClaims(token);

        assertEquals("GERENTE_DE_OPERACIONES", claims.get("role"));
        assertEquals("Juan", claims.get("firstName"));
        assertEquals(2, claims.get("idVenue"));
        assertEquals(List.of("VIEW_DASHBOARD", "MANAGE_MOVIES"), claims.get("permissions"));
    }

    @Test
    void isTokenValid_conTokenManipulado_devuelveFalse() {
        String token = jwtService.generateToken(
                "gerente@cinemax.com", "GERENTE_DE_OPERACIONES", "Juan",
                2, List.of("VIEW_DASHBOARD"));

        String tokenManipulado = token.substring(0, token.length() - 5) + "XXXXX";

        assertFalse(jwtService.isTokenValid(tokenManipulado));
    }

    @Test
    void isTokenValid_conTokenYaExpirado_devuelveFalse() {
        // Expiración negativa = ya vencido desde el momento en que se creó
        ReflectionTestUtils.setField(jwtService, "expirationMillis", -1000L);

        String token = jwtService.generateToken(
                "gerente@cinemax.com", "GERENTE_DE_OPERACIONES", "Juan",
                2, List.of("VIEW_DASHBOARD"));

        assertFalse(jwtService.isTokenValid(token));
    }
}