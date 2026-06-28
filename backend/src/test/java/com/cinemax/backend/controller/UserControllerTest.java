package com.cinemax.backend.controller;

import com.cinemax.backend.model.dto.user.UserCreateDTO;
import com.cinemax.backend.model.dto.user.UserResponseDTO;
import com.cinemax.backend.model.dto.user.UserRoleUpdateDTO;
import com.cinemax.backend.service.user.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private UserResponseDTO usuarioMock;
    private UserCreateDTO requestValido;

    @BeforeEach
    void setUp() {
        usuarioMock = UserResponseDTO.builder()
                .idUser(1)
                .firstName("Carlos")
                .lastName("Ramos")
                .email("carlos.ramos@cinemax.com")
                .roleName("GERENTE_DE_OPERACIONES")
                .status("Activo")
                .build();

        requestValido = UserCreateDTO.builder()
                .firstName("Carlos").lastName("Ramos")
                .email("carlos.ramos@cinemax.com").password("Abcd1234")
                .idRole(5).idVenue(1)
                .documentNumber("70123456").idDocumentType(1)
                .build();
    }

    // ── getAllUsers() ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getAllUsers()")
    class GetAllUsers {

        @Test
        @DisplayName("Retorna 200 con la lista de usuarios cuando el Service responde correctamente")
        void retorna200ConListaDeUsuarios() {
            when(userService.getAllUsers()).thenReturn(List.of(usuarioMock));

            ResponseEntity<List<UserResponseDTO>> response = userController.getAllUsers();

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(1);
            assertThat(response.getBody().get(0).getEmail()).isEqualTo("carlos.ramos@cinemax.com");
            verify(userService, times(1)).getAllUsers();
        }

        @Test
        @DisplayName("Retorna 200 con lista vacía si no hay usuarios")
        void retorna200ConListaVacia() {
            when(userService.getAllUsers()).thenReturn(List.of());

            ResponseEntity<List<UserResponseDTO>> response = userController.getAllUsers();

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEmpty();
        }
    }

    // ── createUser() ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("createUser()")
    class CreateUser {

        @Test
        @DisplayName("Retorna 200 con el usuario creado cuando el Service responde correctamente")
        void retorna200ConUsuarioCreado() {
            when(userService.createUser(any(UserCreateDTO.class))).thenReturn(usuarioMock);

            ResponseEntity<UserResponseDTO> response = userController.createUser(requestValido);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getEmail()).isEqualTo("carlos.ramos@cinemax.com");
            verify(userService, times(1)).createUser(requestValido);
        }

        @Test
        @DisplayName("Propaga la excepción si el Service rechaza la creación (email duplicado)")
        void propagaExcepcionSiEmailDuplicado() {
            when(userService.createUser(any(UserCreateDTO.class)))
                    .thenThrow(new RuntimeException("Error: El correo ya está registrado."));

            org.assertj.core.api.Assertions.assertThatThrownBy(
                            () -> userController.createUser(requestValido))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("correo ya está registrado");
        }
    }

    // ── updateUserRole() ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("updateUserRole()")
    class UpdateUserRole {

        @Test
        @DisplayName("Retorna 200 con el usuario actualizado cuando el Service responde correctamente")
        void retorna200ConUsuarioActualizado() {
            UserRoleUpdateDTO rolUpdate = new UserRoleUpdateDTO();
            rolUpdate.setIdRole(3);
            rolUpdate.setIdVenue(1);

            UserResponseDTO actualizado = UserResponseDTO.builder()
                    .idUser(1).email("carlos.ramos@cinemax.com")
                    .roleName("GERENTE_DE_MARKETING").build();

            when(userService.updateUserRole(eq(1), any(UserRoleUpdateDTO.class)))
                    .thenReturn(actualizado);

            ResponseEntity<UserResponseDTO> response = userController.updateUserRole(1, rolUpdate);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().getRoleName()).isEqualTo("GERENTE_DE_MARKETING");
            verify(userService, times(1)).updateUserRole(1, rolUpdate);
        }
    }

    // ── deleteUser() ──────────────────────────────────────────────────────────

    @Nested
    @DisplayName("deleteUser()")
    class DeleteUser {

        @Test
        @DisplayName("Retorna 204 No Content y llama al Service con el id correcto")
        void retorna204YLlamaAlService() {
            doNothing().when(userService).deleteUser(5);

            ResponseEntity<Void> response = userController.deleteUser(5);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            verify(userService, times(1)).deleteUser(5);
        }
    }

    // ── activateUser() ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("activateUser()")
    class ActivateUser {

        @Test
        @DisplayName("Retorna 200 con mensaje de éxito y llama al Service con el id correcto")
        void retorna200ConMensajeDeExito() {
            doNothing().when(userService).activateUser(5);

            ResponseEntity<Map<String, String>> response = userController.activateUser(5);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).containsEntry("message", "Usuario activado exitosamente");
            verify(userService, times(1)).activateUser(5);
        }
    }

    // ── getMyProfile() ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getMyProfile()")
    class GetMyProfile {

        @Test
        @DisplayName("Extrae el email del Principal y retorna 200 con el perfil del usuario")
        void retorna200ConPerfilDelUsuario() {
            Principal principal = () -> "cliente@cinemax.com";

            UserResponseDTO perfil = UserResponseDTO.builder()
                    .idUser(10).email("cliente@cinemax.com").firstName("María").build();

            when(userService.getMyProfile("cliente@cinemax.com")).thenReturn(perfil);

            ResponseEntity<UserResponseDTO> response = userController.getMyProfile(principal);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().getEmail()).isEqualTo("cliente@cinemax.com");
            // Verifica que el Controller pasa el email del Principal al Service, no un valor hardcodeado
            verify(userService, times(1)).getMyProfile("cliente@cinemax.com");
        }
    }

    // ── deleteMyAccount() ────────────────────────────────────────────────────

    @Nested
    @DisplayName("deleteMyAccount()")
    class DeleteMyAccount {

        @Test
        @DisplayName("Extrae el email del Principal, llama al Service y retorna 204")
        void retorna204YLlamaAlService() {
            Principal principal = () -> "cliente@cinemax.com";
            doNothing().when(userService).deleteMyAccount("cliente@cinemax.com");

            ResponseEntity<Void> response = userController.deleteMyAccount(principal);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            verify(userService, times(1)).deleteMyAccount("cliente@cinemax.com");
        }

        @Test
        @DisplayName("Propaga la excepción si el Service rechaza (colaborador no puede autoeliminarse)")
        void propagaExcepcionSiColaboradorIntentaAutoeliminarse() {
            Principal principal = () -> "gerente@cinemax.com";
            doThrow(new RuntimeException("Los colaboradores no pueden eliminar su propia cuenta."))
                    .when(userService).deleteMyAccount("gerente@cinemax.com");

            org.assertj.core.api.Assertions.assertThatThrownBy(
                            () -> userController.deleteMyAccount(principal))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("no pueden eliminar su propia cuenta");
        }
    }
}