package com.cinemax.backend.service.auth;

import com.cinemax.backend.model.dto.auth.AuthRequestDTO;
import com.cinemax.backend.model.dto.auth.AuthResponseDTO;
import com.cinemax.backend.model.dto.auth.RegisterRequestDTO;
import com.cinemax.backend.model.entity.DocumentType;
import com.cinemax.backend.model.entity.Role;
import com.cinemax.backend.model.entity.UserAccount;
import com.cinemax.backend.repository.DocumentTypeRepository;
import com.cinemax.backend.repository.RoleRepository;
import com.cinemax.backend.repository.UserAccountRepository;
import com.cinemax.backend.security.JwtService;
import com.cinemax.backend.service.email.EmailService;
import com.cinemax.backend.util.RoleConstants;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas Unitarias para AuthServiceImpl - CINEMAX
 * Módulo: Auth & Seguridad (Integrante 1)
 *
 * Cubre los CPs de la Matriz de Caja Negra:
 *   CP-01 Login exitoso
 *   CP-07 Login usuario Inactivo (delegado al AuthenticationManager via Spring Security)
 *   CP-08 Registro exitoso
 *   CP-09 Registro con email duplicado
 *   CP-11 Reset password con token válido
 *   CP-12 Reset password con token expirado
 *   CP-13 Reset password con token inválido
 *   CP-14 requestPasswordReset con email inexistente (silent fail)
 *   CP-15 requestPasswordReset con email existente
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthServiceImpl - Pruebas Unitarias")
class AuthServiceImplTest {

    // ── Dependencias mockeadas ────────────────────────────────────────────────
    @Mock private UserAccountRepository userRepository;
    @Mock private JwtService jwtService;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private RoleRepository roleRepository;
    @Mock private EmailService emailService;
    @Mock private DocumentTypeRepository documentTypeRepository;

    @InjectMocks
    private AuthServiceImpl authService;

    // ── Objetos de prueba reutilizables ───────────────────────────────────────
    private UserAccount activeUser;
    private UserAccount inactiveUser;
    private Role roleCliente;
    private DocumentType docType;

    @BeforeEach
    void setUp() {
        roleCliente = Role.builder()
                .idRole(1)
                .roleName(RoleConstants.CLIENTE)
                .build();

        docType = new DocumentType();
        // Si DocumentType no tiene builder, usamos setter. Ajusta según tu entidad.
        // docType.setIdDocumentType(1);

        activeUser = UserAccount.builder()
                .idUser(1)
                .firstName("Juan")
                .lastName("Pérez")
                .email("juan@test.com")
                .passwordHash("$2a$10$hashedPassword")
                .status("Activo")
                .role(roleCliente)
                .documentNumber("12345678")
                .documentType(docType)
                .build();

        inactiveUser = UserAccount.builder()
                .idUser(2)
                .firstName("María")
                .lastName("García")
                .email("maria@test.com")
                .passwordHash("$2a$10$hashedPassword")
                .status("Inactivo")
                .role(roleCliente)
                .documentNumber("87654321")
                .documentType(docType)
                .build();
    }

    // =========================================================================
    // TESTS DE LOGIN
    // =========================================================================

    @Test
    @DisplayName("CP-01: Login exitoso con credenciales válidas y usuario Activo")
    void login_credencialesValidas_retornaTokenJWT() {
        // ARRANGE
        AuthRequestDTO request = AuthRequestDTO.builder()
                .email("juan@test.com")
                .password("Password1")
                .build();

        when(userRepository.findByEmail("juan@test.com")).thenReturn(Optional.of(activeUser));
        when(jwtService.generateToken(anyMap(), eq(activeUser))).thenReturn("jwt-token-generado");

        // ACT
        AuthResponseDTO response = authService.login(request);

        // ASSERT
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("jwt-token-generado");

        // Verifica que el AuthenticationManager fue invocado con las credenciales correctas
        verify(authenticationManager).authenticate(
                argThat(auth -> auth instanceof UsernamePasswordAuthenticationToken
                        && auth.getPrincipal().equals("juan@test.com")
                        && auth.getCredentials().equals("Password1"))
        );
        // Verifica que el JWT incluye el rol en los extraClaims
        verify(jwtService).generateToken(
                argThat(claims -> claims.containsKey("role") && claims.containsKey("firstName")),
                eq(activeUser)
        );
    }

    @Test
    @DisplayName("CP-02: Login con contraseña incorrecta lanza BadCredentialsException")
    void login_contrasenaIncorrecta_lanzaExcepcion() {
        // ARRANGE
        AuthRequestDTO request = AuthRequestDTO.builder()
                .email("juan@test.com")
                .password("ContraseñaWrong1")
                .build();

        doThrow(new BadCredentialsException("Bad credentials"))
                .when(authenticationManager).authenticate(any());

        // ACT & ASSERT
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);

        // El repositorio NO debe consultarse si falla la autenticación
        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    @DisplayName("CP-07: Login con usuario Inactivo — Spring Security lanza DisabledException o LockedException")
    void login_usuarioInactivo_lanzaExcepcionDeSeguridad() {
        // ARRANGE
        // isEnabled() e isAccountNonLocked() retornan false cuando status="Inactivo".
        // El AuthenticationManager de Spring Security detecta esto y lanza la excepción
        // ANTES de que el servicio pueda continuar.
        AuthRequestDTO request = AuthRequestDTO.builder()
                .email("maria@test.com")
                .password("Password1")
                .build();

        // Simulamos que el AuthenticationManager lanza DisabledException
        // (Spring Security lo hace automáticamente gracias a isEnabled()=false)
        doThrow(new DisabledException("User is disabled"))
                .when(authenticationManager).authenticate(any());

        // ACT & ASSERT
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(DisabledException.class)
                .hasMessageContaining("disabled");

        // El token JWT nunca debe generarse para un usuario inactivo
        verify(jwtService, never()).generateToken(anyMap(), any(UserAccount.class));
    }

    @Test
    @DisplayName("CP-07b: Login con usuario Inactivo — también puede lanzar LockedException vía isAccountNonLocked()")
    void login_usuarioInactivo_puedeLanzarLockedException() {
        // ARRANGE
        AuthRequestDTO request = AuthRequestDTO.builder()
                .email("maria@test.com")
                .password("Password1")
                .build();

        doThrow(new LockedException("User account is locked"))
                .when(authenticationManager).authenticate(any());

        // ACT & ASSERT
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(LockedException.class);

        verify(jwtService, never()).generateToken(anyMap(), any(UserAccount.class));
    }

    // =========================================================================
    // TESTS DE REGISTRO
    // =========================================================================

    @Test
    @DisplayName("CP-08: Registro exitoso con todos los datos válidos")
    void register_datosValidos_guardaUsuarioYretornaToken() {
        // ARRANGE
        RegisterRequestDTO request = RegisterRequestDTO.builder()
                .firstName("Ana")
                .lastName("López")
                .email("ana@test.com")
                .password("Password1")
                .documentNumber("11223344")
                .idDocumentType(1)
                .build();

        when(userRepository.existsByEmail("ana@test.com")).thenReturn(false);
        when(roleRepository.findByRoleName(RoleConstants.CLIENTE)).thenReturn(Optional.of(roleCliente));
        when(documentTypeRepository.findById(1)).thenReturn(Optional.of(docType));
        when(passwordEncoder.encode("Password1")).thenReturn("$2a$10$encodedPassword");
        when(jwtService.generateToken(any(UserAccount.class))).thenReturn("jwt-register-token");

        // ACT
        AuthResponseDTO response = authService.register(request);

        // ASSERT
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("jwt-register-token");

        // Capturamos el objeto guardado para verificar su estado
        ArgumentCaptor<UserAccount> userCaptor = ArgumentCaptor.forClass(UserAccount.class);
        verify(userRepository).save(userCaptor.capture());

        UserAccount savedUser = userCaptor.getValue();
        assertThat(savedUser.getEmail()).isEqualTo("ana@test.com");
        assertThat(savedUser.getStatus()).isEqualTo("Activo");
        assertThat(savedUser.getPasswordHash()).isEqualTo("$2a$10$encodedPassword");
        assertThat(savedUser.getRole()).isEqualTo(roleCliente);
    }

    @Test
    @DisplayName("CP-09: Registro con email duplicado lanza RuntimeException")
    void register_emailDuplicado_lanzaExcepcion() {
        // ARRANGE
        RegisterRequestDTO request = RegisterRequestDTO.builder()
                .firstName("Juan")
                .lastName("Pérez")
                .email("juan@test.com") // ya existe
                .password("Password1")
                .documentNumber("12345678")
                .idDocumentType(1)
                .build();

        when(userRepository.existsByEmail("juan@test.com")).thenReturn(true);

        // ACT & ASSERT
        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("correo electrónico ya se encuentra registrado");

        // El usuario nunca debe guardarse
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("CP-10: Registro cuando el rol CLIENTE no existe en BD lanza RuntimeException")
    void register_rolClienteNoEncontrado_lanzaExcepcion() {
        // ARRANGE
        RegisterRequestDTO request = RegisterRequestDTO.builder()
                .firstName("Carlos")
                .lastName("Ruiz")
                .email("carlos@test.com")
                .password("Password1")
                .documentNumber("99887766")
                .idDocumentType(1)
                .build();

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(roleRepository.findByRoleName(RoleConstants.CLIENTE)).thenReturn(Optional.empty());

        // ACT & ASSERT
        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Rol CLIENTE no encontrado");

        verify(userRepository, never()).save(any());
    }

    // =========================================================================
    // TESTS DE RESET PASSWORD
    // =========================================================================

    @Test
    @DisplayName("CP-14: requestPasswordReset con email inexistente — falla silenciosa (no lanza excepción)")
    void requestPasswordReset_emailInexistente_noLanzaExcepcionYNoGuarda() {
        // ARRANGE
        when(userRepository.findByEmail("noexiste@test.com")).thenReturn(Optional.empty());

        // ACT — no debe lanzar excepción (comportamiento deliberado para evitar user enumeration)
        authService.requestPasswordReset("noexiste@test.com");

        // ASSERT
        verify(userRepository, never()).save(any());
        // No enviamos email si el correo no existe
        // (verificación asíncrona no aplica pues el método retorna antes)
    }

    @Test
    @DisplayName("CP-15: requestPasswordReset con email válido guarda el token y la fecha de expiración")
    void requestPasswordReset_emailExistente_guardaTokenEnUsuario() {
        // ARRANGE
        when(userRepository.findByEmail("juan@test.com")).thenReturn(Optional.of(activeUser));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // ACT
        authService.requestPasswordReset("juan@test.com");

        // ASSERT
        ArgumentCaptor<UserAccount> captor = ArgumentCaptor.forClass(UserAccount.class);
        verify(userRepository).save(captor.capture());

        UserAccount saved = captor.getValue();
        assertThat(saved.getResetToken()).isNotNull().isNotBlank();
        assertThat(saved.getTokenExpiryDate()).isNotNull();
        // El token debe expirar en el futuro (aprox. 1 minuto)
        assertThat(saved.getTokenExpiryDate()).isAfter(LocalDateTime.now());
    }

    @Test
    @DisplayName("CP-11: resetPassword con token válido y no expirado actualiza la contraseña")
    void resetPassword_tokenValido_actualizaPasswordYLimpiaToken() {
        // ARRANGE
        activeUser.setResetToken("token-valido-uuid");
        activeUser.setTokenExpiryDate(LocalDateTime.now().plusMinutes(1)); // no expirado

        when(userRepository.findByResetToken("token-valido-uuid")).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.encode("NuevaPass1")).thenReturn("$2a$10$newHashedPassword");
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // ACT
        authService.resetPassword("token-valido-uuid", "NuevaPass1");

        // ASSERT
        ArgumentCaptor<UserAccount> captor = ArgumentCaptor.forClass(UserAccount.class);
        verify(userRepository).save(captor.capture());

        UserAccount saved = captor.getValue();
        assertThat(saved.getPasswordHash()).isEqualTo("$2a$10$newHashedPassword");
        assertThat(saved.getResetToken()).isNull();
        assertThat(saved.getTokenExpiryDate()).isNull();
    }

    @Test
    @DisplayName("CP-12: resetPassword con token expirado lanza RuntimeException")
    void resetPassword_tokenExpirado_lanzaExcepcion() {
        // ARRANGE
        activeUser.setResetToken("token-expirado-uuid");
        activeUser.setTokenExpiryDate(LocalDateTime.now().minusMinutes(5)); // ya expiró

        when(userRepository.findByResetToken("token-expirado-uuid")).thenReturn(Optional.of(activeUser));

        // ACT & ASSERT
        assertThatThrownBy(() -> authService.resetPassword("token-expirado-uuid", "NuevaPass1"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("expirado");

        // La contraseña NO debe actualizarse
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("CP-13: resetPassword con token inválido (no existe) lanza RuntimeException")
    void resetPassword_tokenInvalido_lanzaExcepcion() {
        // ARRANGE
        when(userRepository.findByResetToken("token-inexistente")).thenReturn(Optional.empty());

        // ACT & ASSERT
        assertThatThrownBy(() -> authService.resetPassword("token-inexistente", "NuevaPass1"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Token inválido");

        verify(userRepository, never()).save(any());
    }

    // =========================================================================
    // TESTS DE validateResetToken
    // =========================================================================

    @Test
    @DisplayName("validateResetToken con token válido y vigente retorna true")
    void validateResetToken_tokenVigenteYExistente_retornaTrue() {
        // ARRANGE
        activeUser.setResetToken("token-vigente");
        activeUser.setTokenExpiryDate(LocalDateTime.now().plusMinutes(1));

        when(userRepository.findByResetToken("token-vigente")).thenReturn(Optional.of(activeUser));

        // ACT
        boolean result = authService.validateResetToken("token-vigente");

        // ASSERT
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("validateResetToken con token expirado retorna false")
    void validateResetToken_tokenExpirado_retornaFalse() {
        // ARRANGE
        activeUser.setResetToken("token-expirado");
        activeUser.setTokenExpiryDate(LocalDateTime.now().minusMinutes(10));

        when(userRepository.findByResetToken("token-expirado")).thenReturn(Optional.of(activeUser));

        // ACT
        boolean result = authService.validateResetToken("token-expirado");

        // ASSERT
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("validateResetToken con token inexistente retorna false")
    void validateResetToken_tokenInexistente_retornaFalse() {
        // ARRANGE
        when(userRepository.findByResetToken("no-existe")).thenReturn(Optional.empty());

        // ACT
        boolean result = authService.validateResetToken("no-existe");

        // ASSERT
        assertThat(result).isFalse();
    }
}
