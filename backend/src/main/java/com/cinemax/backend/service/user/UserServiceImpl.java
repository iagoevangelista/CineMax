package com.cinemax.backend.service.user;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cinemax.backend.model.dto.user.UserCreateDTO;
import com.cinemax.backend.model.dto.user.UserResponseDTO;
import com.cinemax.backend.model.dto.user.UserRoleUpdateDTO;
import com.cinemax.backend.model.entity.DocumentType;
import com.cinemax.backend.model.entity.Role;
import com.cinemax.backend.model.entity.UserAccount;
import com.cinemax.backend.model.entity.Venue;
import com.cinemax.backend.repository.RoleRepository;
import com.cinemax.backend.repository.UserAccountRepository;
import com.cinemax.backend.repository.VenueRepository;
import com.cinemax.backend.service.cloudinary.CloudinaryService;
import com.cinemax.backend.model.dto.user.UserUpdateDTO;
import com.cinemax.backend.util.RoleConstants;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserAccountRepository userRepository;
    private final RoleRepository roleRepository;
    private final VenueRepository venueRepository;
    private final PasswordEncoder passwordEncoder;
    private final CloudinaryService cloudinaryService;

    private boolean rolRequiereSede(String roleName) {
        return RoleConstants.GERENTE_MARKETING.equals(roleName)
                || RoleConstants.GERENTE_OPERACIONES.equals(roleName);
    }

    private boolean rolEsUnicoGlobal(String roleName) {
        return RoleConstants.GERENTE_GENERAL.equals(roleName);
    }

    @Override
    public List<UserResponseDTO> getAllUsers() {
        List<UserAccount> usuarios = userRepository.findAll();
        List<UserResponseDTO> listaResponse = new ArrayList<>();

        for (UserAccount usuario : usuarios) {
            // Excluir clientes del panel administrativo
            if (usuario.getRole() != null
                    && RoleConstants.CLIENTE.equals(usuario.getRole().getRoleName())) {
                continue;
            }

            listaResponse.add(mapToResponseDTO(usuario));
        }
        return listaResponse;
    }

    @Override
    public UserResponseDTO createUser(UserCreateDTO request) {

        Role rol = roleRepository.findById(request.getIdRole())
                .orElseThrow(() -> new RuntimeException("Rol no encontrado."));

        String roleName = rol.getRoleName();

        if (rolEsUnicoGlobal(roleName)) {
            if (userRepository.existsByRole_IdRoleAndStatus(rol.getIdRole(), "Activo")) {
                throw new RuntimeException("Error: Ya existe un Gerente General activo en CineMax.");
            }
            request.setIdVenue(null);
        }
        // Roles que requieren sede obligatoria
        else if (rolRequiereSede(roleName)) {
            if (request.getIdVenue() == null || request.getIdVenue() == 0) {
                throw new RuntimeException("Error: Este rol requiere ser asignado a una Sede.");
            }
            if (userRepository.existsByRole_IdRoleAndVenue_IdVenueAndStatus(
                    rol.getIdRole(), request.getIdVenue(), "Activo")) {
                throw new RuntimeException("Error: La sede seleccionada ya tiene un gerente activo de este tipo.");
            }
        }

        UserAccount nuevoUsuario = new UserAccount();
        nuevoUsuario.setFirstName(request.getFirstName());
        nuevoUsuario.setLastName(request.getLastName());
        nuevoUsuario.setEmail(request.getEmail());
        nuevoUsuario.setStatus("Activo");
        nuevoUsuario.setDocumentNumber(request.getDocumentNumber());
        nuevoUsuario.setDocumentType(DocumentType.builder().idDocumentType(request.getIdDocumentType()).build());
        nuevoUsuario.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        nuevoUsuario.setRole(rol);

        if (request.getIdVenue() != null && request.getIdVenue() > 0) {
            Venue sede = venueRepository.findById(request.getIdVenue())
                    .orElseThrow(() -> new RuntimeException("Sede no encontrada."));
            nuevoUsuario.setVenue(sede);
        }

        UserAccount guardado = userRepository.save(nuevoUsuario);
        return mapToResponseDTO(guardado);
    }

    @Override
    public UserResponseDTO updateUserRole(Integer idUser, UserRoleUpdateDTO request) {
        UserAccount usuarioExistente = userRepository.findById(idUser)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));

        Role nuevoRol = roleRepository.findById(request.getIdRole())
                .orElseThrow(() -> new RuntimeException("Rol no encontrado."));

        String roleName = nuevoRol.getRoleName();

        if (rolEsUnicoGlobal(roleName)) {
            UserAccount gerenteActual = userRepository
                    .findByRole_IdRoleAndStatus(nuevoRol.getIdRole(), "Activo")
                    .orElse(null);
            if (gerenteActual != null && !gerenteActual.getIdUser().equals(idUser)) {
                throw new RuntimeException("Error: Ya existe un Gerente General activo en CineMax.");
            }
            usuarioExistente.setVenue(null);
        }
        else if (rolRequiereSede(roleName)) {
            if (request.getIdVenue() == null || request.getIdVenue() == 0) {
                throw new RuntimeException("Error: Este rol requiere ser asignado a una Sede.");
            }
            UserAccount gerenteSede = userRepository.findByRole_IdRoleAndVenue_IdVenueAndStatus(
                    nuevoRol.getIdRole(), request.getIdVenue(), "Activo").orElse(null);

            if (gerenteSede != null && !gerenteSede.getIdUser().equals(idUser)) {
                throw new RuntimeException("Error: La sede seleccionada ya tiene un gerente activo de este tipo.");
            }

            Venue nuevaSede = venueRepository.findById(request.getIdVenue())
                    .orElseThrow(() -> new RuntimeException("Sede no encontrada."));
            usuarioExistente.setVenue(nuevaSede);
        }
        else {
            usuarioExistente.setVenue(null);
        }

        usuarioExistente.setRole(nuevoRol);
        UserAccount usuarioActualizado = userRepository.save(usuarioExistente);
        return mapToResponseDTO(usuarioActualizado);
    }

    @Override
    public void deleteUser(Integer idUser) {
        UserAccount usuario = userRepository.findById(idUser)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));
        usuario.setStatus("Inactivo");
        userRepository.save(usuario);
    }

    @Override
    public void activateUser(Integer idUser) {
        UserAccount user = userRepository.findById(idUser)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));

        if (user.getRole() != null) {
            String roleName = user.getRole().getRoleName();
            Integer idRole = user.getRole().getIdRole();

            if (rolEsUnicoGlobal(roleName)) {
                if (userRepository.existsByRole_IdRoleAndStatus(idRole, "Activo")) {
                    throw new RuntimeException(
                            "No se puede reactivar: Ya existe un Gerente General activo actualmente en el sistema.");
                }
            }
            else if (rolRequiereSede(roleName)) {
                if (user.getVenue() != null) {
                    Integer idVenue = user.getVenue().getIdVenue();
                    if (userRepository.existsByRole_IdRoleAndVenue_IdVenueAndStatus(idRole, idVenue, "Activo")) {
                        throw new RuntimeException(
                                "No se puede reactivar: La sede " + user.getVenue().getNameVenue()
                                        + " ya está ocupada por otro gerente activo.");
                    }
                }
            }
        }

        user.setStatus("Activo");
        userRepository.save(user);
    }

    @Override
    public UserResponseDTO getMyProfile(String email) {
        UserAccount user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));

        UserResponseDTO response = new UserResponseDTO();
        response.setIdUser(user.getIdUser());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setEmail(user.getEmail());
        response.setStatus(user.getStatus());
        response.setDocumentNumber(user.getDocumentNumber());
        response.setPhone(user.getPhoneNumber());
        response.setDatebirth(user.getBirthDate());
        response.setImageUrl(user.getImageUrl());

        if (user.getRole() != null) {
            response.setIdRole(user.getRole().getIdRole());
            response.setRoleName(user.getRole().getRoleName());
        }
        if (user.getVenue() != null) {
            response.setIdVenue(user.getVenue().getIdVenue());
            response.setVenueName(user.getVenue().getNameVenue());
        }

        return response;
    }

    @Override
    public void deleteMyAccount(String email) {
        UserAccount user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));

        // Solo un CLIENTE puede autoeliminarse.
        String roleName = user.getRole() != null ? user.getRole().getRoleName() : null;
        if (!RoleConstants.CLIENTE.equals(roleName)) {
            throw new RuntimeException(
                    "Los colaboradores no pueden eliminar su propia cuenta. Solicita la baja a un administrador.");
        }

        user.setStatus("Inactivo");
        userRepository.save(user);
    }

    @Override
    public UserResponseDTO updateProfile(UserUpdateDTO updateDTO, MultipartFile image, String email) {
        UserAccount user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));

        if (image != null && !image.isEmpty()) {
            try {
                String uploadedUrl = cloudinaryService.uploadImage(image);
                user.setImageUrl(uploadedUrl);
            } catch (Exception e) {
                throw new RuntimeException("Error al subir imagen a Cloudinary.");
            }
        }

        if (updateDTO.getFirstName() != null) user.setFirstName(updateDTO.getFirstName());
        if (updateDTO.getLastName() != null) user.setLastName(updateDTO.getLastName());
        if (updateDTO.getPhone() != null) user.setPhoneNumber(updateDTO.getPhone());
        if (updateDTO.getDatebirth() != null) user.setBirthDate(updateDTO.getDatebirth());

        if (updateDTO.getNewPassword() != null && !updateDTO.getNewPassword().isEmpty()) {
            if (updateDTO.getOldPassword() == null
                    || !passwordEncoder.matches(updateDTO.getOldPassword(), user.getPasswordHash())) {
                throw new RuntimeException("Contraseña actual incorrecta.");
            }
            user.setPasswordHash(passwordEncoder.encode(updateDTO.getNewPassword()));
        }

        UserAccount updatedUser = userRepository.save(user);
        return mapToResponseDTO(updatedUser);
    }

    private UserResponseDTO mapToResponseDTO(UserAccount usuario) {
        UserResponseDTO response = new UserResponseDTO();
        response.setIdUser(usuario.getIdUser());
        response.setFirstName(usuario.getFirstName());
        response.setLastName(usuario.getLastName());
        response.setEmail(usuario.getEmail());
        response.setStatus(usuario.getStatus());
        response.setDocumentNumber(usuario.getDocumentNumber());
        response.setPhone(usuario.getPhoneNumber());
        response.setDatebirth(usuario.getBirthDate());
        response.setImageUrl(usuario.getImageUrl());

        if (usuario.getRole() != null) {
            response.setRoleName(usuario.getRole().getRoleName());
            response.setIdRole(usuario.getRole().getIdRole());
        }
        if (usuario.getVenue() != null) {
            response.setIdVenue(usuario.getVenue().getIdVenue());
            response.setVenueName(usuario.getVenue().getNameVenue());
        }
        return response;
    }
}