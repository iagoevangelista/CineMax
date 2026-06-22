package com.cinemax.backend.service.user;

import com.cinemax.backend.model.dto.user.UserCreateDTO;
import com.cinemax.backend.model.dto.user.UserResponseDTO;
import com.cinemax.backend.model.entity.Role;
import com.cinemax.backend.model.entity.UserAccount;
import com.cinemax.backend.model.entity.Venue;
import com.cinemax.backend.repository.RoleRepository;
import com.cinemax.backend.repository.UserAccountRepository;
import com.cinemax.backend.repository.VenueRepository;
import com.cinemax.backend.service.cloudinary.CloudinaryService;
import com.cinemax.backend.util.RoleConstants;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserAccountRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private VenueRepository venueRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private CloudinaryService cloudinaryService;

    private UserServiceImpl userService;

    // Datos reutilizables entre pruebas
    private Role rolGerenteOperaciones;
    private Role rolGerenteGeneral;
    private Venue sedeSanIsidro;
    private UserCreateDTO requestValido;

    @BeforeEach
    void setUp() {
        // El constructor coincide con el orden de los "private final" de UserServiceImpl
        userService = new UserServiceImpl(
                userRepository, roleRepository, venueRepository, passwordEncoder, cloudinaryService
        );

        rolGerenteOperaciones = Role.builder()
                .idRole(5)
                .roleName(RoleConstants.GERENTE_OPERACIONES)
                .build();

        rolGerenteGeneral = Role.builder()
                .idRole(2)
                .roleName(RoleConstants.GERENTE_GENERAL)
                .build();

        Role rolAdmin = Role.builder()
        .idRole(1)
        .roleName(RoleConstants.ADMIN)
        .build();

        sedeSanIsidro = Venue.builder()
                .idVenue(1)
                .nameVenue("CineMax San Isidro")
                .build();

        requestValido = UserCreateDTO.builder()
                .firstName("Carlos")
                .lastName("Ramos")
                .email("carlos.ramos@cinemax.com")
                .password("Abcd1234")
                .idRole(5)
                .idVenue(1)
                .documentNumber("70123456")
                .idDocumentType(1)
                .build();
    }

    @Nested
    @DisplayName("createUser()")
    class CreateUser {

        @Test
        @DisplayName("CP-USR-01: crea un colaborador cuando todos los datos son válidos")
        void creaColaboradorConDatosValidos() {
            // Arrange: el rol existe, ni el email ni el documento están repetidos,
            // la sede existe y no tiene ya un gerente de operaciones activo.
            when(roleRepository.findById(5)).thenReturn(Optional.of(rolGerenteOperaciones));
            when(userRepository.existsByEmail("carlos.ramos@cinemax.com")).thenReturn(false);
            when(userRepository.existsByDocumentNumber("70123456")).thenReturn(false);
            when(userRepository.existsByRole_IdRoleAndVenue_IdVenueAndStatus(5, 1, "Activo")).thenReturn(false);
            when(venueRepository.findById(1)).thenReturn(Optional.of(sedeSanIsidro));
            when(passwordEncoder.encode("Abcd1234")).thenReturn("HASH_FALSO");

            // Se devuelve el mismo objeto que recibió save(), con un id simulado
            when(userRepository.save(any(UserAccount.class))).thenAnswer(invocacion -> {
                UserAccount guardado = invocacion.getArgument(0);
                guardado.setIdUser(99);
                return guardado;
            });

            // Act
            UserResponseDTO resultado = userService.createUser(requestValido);

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(resultado.getEmail()).isEqualTo("carlos.ramos@cinemax.com");
            verify(userRepository, times(1)).save(any(UserAccount.class));
        }

        @Test
        @DisplayName("CP-USR-02 / CP-USR-13: rechaza la creación si el email ya existe")
        void rechazaEmailDuplicado() {
            when(roleRepository.findById(5)).thenReturn(Optional.of(rolGerenteOperaciones));
            when(userRepository.existsByEmail("carlos.ramos@cinemax.com")).thenReturn(true);

            // Act + Assert: esperamos que lance una RuntimeException con un mensaje específico.
            assertThatThrownBy(() -> userService.createUser(requestValido))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("correo ya está registrado");

            // Verificamos que NUNCA se llegó a guardar nada en la base de datos.
            verify(userRepository, never()).save(any(UserAccount.class));
        }

        @Test
        @DisplayName("CP-USR-12: rechaza la creación si el número de documento ya existe")
        void rechazaDocumentoDuplicado() {
            when(roleRepository.findById(5)).thenReturn(Optional.of(rolGerenteOperaciones));
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(userRepository.existsByDocumentNumber("70123456")).thenReturn(true);

            assertThatThrownBy(() -> userService.createUser(requestValido))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("documento ya está registrado");

            verify(userRepository, never()).save(any(UserAccount.class));
        }

        @Test
        @DisplayName("CP-USR-03: rechaza un Gerente de Operaciones sin sede asignada")
        void rechazaRolSinSedeObligatoria() {
            requestValido.setIdVenue(0);

            when(roleRepository.findById(5)).thenReturn(Optional.of(rolGerenteOperaciones));
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(userRepository.existsByDocumentNumber(anyString())).thenReturn(false);

            assertThatThrownBy(() -> userService.createUser(requestValido))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("requiere ser asignado a una Sede");

            verify(userRepository, never()).save(any(UserAccount.class));
        }

        @Test
        @DisplayName("CP-USR-04: rechaza un segundo Gerente General activo")
        void rechazaSegundoGerenteGeneralActivo() {
            requestValido.setIdRole(2);
            requestValido.setIdVenue(null);

            when(roleRepository.findById(2)).thenReturn(Optional.of(rolGerenteGeneral));
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(userRepository.existsByDocumentNumber(anyString())).thenReturn(false);
            when(userRepository.existsByRole_IdRoleAndStatus(2, "Activo")).thenReturn(true);

            assertThatThrownBy(() -> userService.createUser(requestValido))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Ya existe un Gerente General activo");

            verify(userRepository, never()).save(any(UserAccount.class));
        }

        @Test
        @DisplayName("Sede ya tiene un gerente activo del mismo tipo: se rechaza")
        void rechazaSedeConGerenteActivoDelMismoTipo() {
            when(roleRepository.findById(5)).thenReturn(Optional.of(rolGerenteOperaciones));
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(userRepository.existsByDocumentNumber(anyString())).thenReturn(false);
            when(userRepository.existsByRole_IdRoleAndVenue_IdVenueAndStatus(5, 1, "Activo")).thenReturn(true);

            assertThatThrownBy(() -> userService.createUser(requestValido))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("ya tiene un gerente activo");

            verify(userRepository, never()).save(any(UserAccount.class));
        }

        @Test
        @DisplayName("Rol inexistente: lanza error claro antes de tocar la BD")
        void rechazaRolInexistente() {
            when(roleRepository.findById(999)).thenReturn(Optional.empty());
            requestValido.setIdRole(999);

            assertThatThrownBy(() -> userService.createUser(requestValido))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Rol no encontrado");

            verify(userRepository, never()).save(any(UserAccount.class));
        }
    }

    @Nested
    @DisplayName("deleteMyAccount()")
    class DeleteMyAccount {

        @Test
        @DisplayName("Un CLIENTE puede eliminar su propia cuenta")
        void clienteEliminaSuPropiaCuenta() {
            Role rolCliente = Role.builder().idRole(4).roleName(RoleConstants.CLIENTE).build();
            UserAccount cliente = UserAccount.builder()
                    .idUser(10)
                    .email("cliente@cinemax.com")
                    .role(rolCliente)
                    .status("Activo")
                    .build();

            when(userRepository.findByEmail("cliente@cinemax.com")).thenReturn(Optional.of(cliente));
            when(userRepository.save(any(UserAccount.class))).thenAnswer(inv -> inv.getArgument(0));

            userService.deleteMyAccount("cliente@cinemax.com");

            assertThat(cliente.getStatus()).isEqualTo("Inactivo");
            verify(userRepository, times(1)).save(cliente);
        }

        @Test
        @DisplayName("Un colaborador NO puede autoeliminarse")
        void colaboradorNoPuedeAutoeliminarse() {
            UserAccount gerente = UserAccount.builder()
                    .idUser(20)
                    .email("gerente@cinemax.com")
                    .role(rolGerenteOperaciones)
                    .status("Activo")
                    .build();

            when(userRepository.findByEmail("gerente@cinemax.com")).thenReturn(Optional.of(gerente));

            assertThatThrownBy(() -> userService.deleteMyAccount("gerente@cinemax.com"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("no pueden eliminar su propia cuenta");

            // El estado del usuario no debe cambiar, y nunca debe llamarse a save().
            assertThat(gerente.getStatus()).isEqualTo("Activo");
            verify(userRepository, never()).save(any(UserAccount.class));
        }

        @Test
        @DisplayName("Lanza error si el email del token no corresponde a ningún usuario")
        void rechazaUsuarioInexistente() {
            when(userRepository.findByEmail("fantasma@cinemax.com")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.deleteMyAccount("fantasma@cinemax.com"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("no encontrado");
        }
    }

    @Nested
    @DisplayName("activateUser()")
    class ActivateUser {

        @Test
        @DisplayName("CP-USR-10: rechaza reactivar un Gerente si ya hay otro activo en la misma sede")
        void rechazaReactivarConGerenteYaActivoEnLaSede() {
            UserAccount inactivo = UserAccount.builder()
                    .idUser(30)
                    .role(rolGerenteOperaciones)
                    .venue(sedeSanIsidro)
                    .status("Inactivo")
                    .build();

            when(userRepository.findById(30)).thenReturn(Optional.of(inactivo));
            when(userRepository.existsByRole_IdRoleAndVenue_IdVenueAndStatus(5, 1, "Activo")).thenReturn(true);

            assertThatThrownBy(() -> userService.activateUser(30))
                    .isInstanceOf(RuntimeException.class);

            assertThat(inactivo.getStatus()).isEqualTo("Inactivo");
            verify(userRepository, never()).save(any(UserAccount.class));
        }
    }
}