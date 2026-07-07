package com.cinemax.backend.controller;

import com.cinemax.backend.model.dto.auth.AuthRequestDTO;
import com.cinemax.backend.model.dto.auth.AuthResponseDTO;
import com.cinemax.backend.model.dto.auth.RegisterRequestDTO;
import com.cinemax.backend.service.auth.AuthService;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.cinemax.backend.exception.GlobalExceptionHandler;

import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController - Pruebas Unitarias (MockMvc Standalone)")
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
        .setControllerAdvice(new GlobalExceptionHandler())
        .build();
        objectMapper = new ObjectMapper();
    }

    // =========================================================================
    // POST /api/v1/auth/login
    // =========================================================================

    @Test
    @DisplayName("POST /login — CP-01: credenciales válidas retorna 200 con token JWT")
    void login_credencialesValidas_retorna200ConToken() throws Exception {
        // ARRANGE
        AuthRequestDTO request = AuthRequestDTO.builder()
                .email("juan@test.com")
                .password("Password1")
                .build();

        AuthResponseDTO response = AuthResponseDTO.builder()
                .token("jwt-token-de-prueba")
                .build();

        when(authService.login(any(AuthRequestDTO.class))).thenReturn(response);

        // ACT & ASSERT
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").value("jwt-token-de-prueba"));

        verify(authService).login(any(AuthRequestDTO.class));
    }

    @Test
    @DisplayName("POST /login — CP-02: credenciales incorrectas propaga excepción del service")
    void login_credencialesIncorrectas_propagaExcepcion() throws Exception {
        AuthRequestDTO request = AuthRequestDTO.builder()
                .email("juan@test.com")
                .password("MalPassword1")
                .build();

        when(authService.login(any(AuthRequestDTO.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // ACT & ASSERT
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(authService).login(any(AuthRequestDTO.class));
    }

    @Test
    @DisplayName("POST /login — CP-07: usuario inactivo propaga DisabledException del service")
    void login_usuarioInactivo_propagaDisabledException() throws Exception {
        // ARRANGE
        AuthRequestDTO request = AuthRequestDTO.builder()
                .email("maria@test.com")
                .password("Password1")
                .build();

        when(authService.login(any(AuthRequestDTO.class)))
                .thenThrow(new DisabledException("User is disabled"));

        // ACT & ASSERT
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(authService).login(any(AuthRequestDTO.class));
    }

    // =========================================================================
    // POST /api/v1/auth/register
    // =========================================================================

    @Test
    @DisplayName("POST /register — CP-08: datos válidos retorna 200 con token JWT")
    void register_datosValidos_retorna200ConToken() throws Exception {
        // ARRANGE
        RegisterRequestDTO request = RegisterRequestDTO.builder()
                .firstName("Ana")
                .lastName("López")
                .email("ana@test.com")
                .password("Password1")
                .documentNumber("11223344")
                .idDocumentType(1)
                .build();

        AuthResponseDTO response = AuthResponseDTO.builder()
                .token("jwt-register-token")
                .build();

        when(authService.register(any(RegisterRequestDTO.class))).thenReturn(response);

        // ACT & ASSERT
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-register-token"));

        verify(authService).register(any(RegisterRequestDTO.class));
    }

    @Test
    @DisplayName("POST /register — CP-09: email duplicado propaga RuntimeException del service")
    void register_emailDuplicado_propagaExcepcion() throws Exception {
        // ARRANGE
        RegisterRequestDTO request = RegisterRequestDTO.builder()
                .firstName("Juan")
                .lastName("Pérez")
                .email("juan@test.com")
                .password("Password1")
                .documentNumber("12345678")
                .idDocumentType(1)
                .build();

        when(authService.register(any(RegisterRequestDTO.class)))
                .thenThrow(new RuntimeException("El correo electrónico ya se encuentra registrado."));

        // ACT & ASSERT
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(authService).register(any(RegisterRequestDTO.class));
    }

    @Test
    @DisplayName("POST /register — Bean Validation: campos obligatorios vacíos retorna 400")
    void register_camposObligatoriosVacios_retorna400() throws Exception {
        // ARRANGE — email y firstName vacíos violan @NotBlank y @Email
        RegisterRequestDTO request = RegisterRequestDTO.builder()
                .firstName("")
                .lastName("López")
                .email("no-es-email")
                .password("Password1")
                .documentNumber("11223344")
                .idDocumentType(1)
                .build();

        // ACT & ASSERT
        // Spring valida el @Valid antes de llamar al service, retorna 400 Bad Request
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        // El service nunca debe ser invocado si la validación falla
        verify(authService, never()).register(any());
    }

    // =========================================================================
    // POST /api/v1/auth/forgot-password
    // =========================================================================

    @Test
    @DisplayName("POST /forgot-password — email existente retorna 200 con mensaje genérico")
    void forgotPassword_emailExistente_retorna200ConMensajeGenerico() throws Exception {
        // ARRANGE
        doNothing().when(authService).requestPasswordReset("juan@test.com");

        Map<String, String> body = Map.of("email", "juan@test.com");

        // ACT & ASSERT
        mockMvc.perform(post("/api/v1/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("Si el correo existe, se ha enviado un enlace de recuperación."));

        verify(authService).requestPasswordReset("juan@test.com");
    }

    @Test
    @DisplayName("POST /forgot-password — email inexistente sigue retornando 200 (no revela existencia)")
    void forgotPassword_emailInexistente_retorna200IgualmenteSinExponerInfo() throws Exception {
        // ARRANGE — el service hace silent fail, no lanza excepción
        doNothing().when(authService).requestPasswordReset("noexiste@test.com");

        Map<String, String> body = Map.of("email", "noexiste@test.com");

        // ACT & ASSERT
        mockMvc.perform(post("/api/v1/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("Si el correo existe, se ha enviado un enlace de recuperación."));
    }

    // =========================================================================
    // POST /api/v1/auth/reset-password
    // =========================================================================

    @Test
    @DisplayName("POST /reset-password — CP-11: token válido retorna 200 con mensaje de éxito")
    void resetPassword_tokenValido_retorna200() throws Exception {
        // ARRANGE
        doNothing().when(authService).resetPassword("token-valido", "NuevaPass1");

        Map<String, String> body = Map.of("token", "token-valido", "newPassword", "NuevaPass1");

        // ACT & ASSERT
        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Contraseña restablecida con éxito."));

        verify(authService).resetPassword("token-valido", "NuevaPass1");
    }

    @Test
    @DisplayName("POST /reset-password — CP-12: token expirado propaga RuntimeException del service")
    void resetPassword_tokenExpirado_propagaExcepcion() throws Exception {
        // ARRANGE
        doThrow(new RuntimeException("El enlace de recuperación ha expirado. Por favor solicita uno nuevo."))
                .when(authService).resetPassword(eq("token-expirado"), anyString());

        Map<String, String> body = Map.of("token", "token-expirado", "newPassword", "NuevaPass1");

        // ACT & ASSERT
        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());

        verify(authService).resetPassword("token-expirado", "NuevaPass1");
    }

    @Test
    @DisplayName("POST /reset-password — CP-13: token inválido propaga RuntimeException del service")
    void resetPassword_tokenInvalido_propagaExcepcion() throws Exception {
        // ARRANGE
        doThrow(new RuntimeException("Token inválido o no existe."))
                .when(authService).resetPassword(eq("token-invalido"), anyString());

        Map<String, String> body = Map.of("token", "token-invalido", "newPassword", "NuevaPass1");

        // ACT & ASSERT
        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    // =========================================================================
    // GET /api/v1/auth/validate-token
    // =========================================================================

    @Test
    @DisplayName("GET /validate-token — token vigente retorna 200 con isValid=true")
    void validateToken_tokenVigente_retornaTrue() throws Exception {
        // ARRANGE
        when(authService.validateResetToken("token-vigente")).thenReturn(true);

        // ACT & ASSERT
        mockMvc.perform(get("/api/v1/auth/validate-token")
                        .param("token", "token-vigente"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isValid").value(true));
    }

    @Test
    @DisplayName("GET /validate-token — token expirado o inexistente retorna 200 con isValid=false")
    void validateToken_tokenInvalido_retornaFalse() throws Exception {
        // ARRANGE
        when(authService.validateResetToken("token-malo")).thenReturn(false);

        // ACT & ASSERT
        mockMvc.perform(get("/api/v1/auth/validate-token")
                        .param("token", "token-malo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isValid").value(false));
    }
}
