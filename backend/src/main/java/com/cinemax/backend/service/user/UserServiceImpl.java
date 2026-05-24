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

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserAccountRepository userRepository;
    private final RoleRepository roleRepository;
    private final VenueRepository venueRepository; 
    private final PasswordEncoder passwordEncoder; 
    private final CloudinaryService cloudinaryService;

    @Override
    public List<UserResponseDTO> getAllUsers() {
        List<UserAccount> usuarios = userRepository.findAll();
        List<UserResponseDTO> listaResponse = new ArrayList<>();

        for (UserAccount usuario : usuarios) {
            if (usuario.getRole() != null && usuario.getRole().getRoleName().equals("CLIENTE")) {
                continue;
            }

            UserResponseDTO dto = new UserResponseDTO();
            dto.setIdUser(usuario.getIdUser());
            dto.setFirstName(usuario.getFirstName());
            dto.setLastName(usuario.getLastName());
            dto.setEmail(usuario.getEmail());
            dto.setStatus(usuario.getStatus());

            if (usuario.getRole() != null) {
                dto.setRoleName(usuario.getRole().getRoleName());
                dto.setIdRole(usuario.getRole().getIdRole());
            }

            if (usuario.getVenue() != null) {
                dto.setIdVenue(usuario.getVenue().getIdVenue()); // <-- AGREGADO
                dto.setVenueName(usuario.getVenue().getNameVenue());
            }

            listaResponse.add(dto);
        }
        return listaResponse;
    }

    @Override
    public UserResponseDTO createUser(UserCreateDTO request) {
        if (request.getIdRole() == 2) {
            if (userRepository.existsByRole_IdRoleAndStatus(2, "Activo")) {
                throw new RuntimeException("Error: Ya existe un Gerente General activo en CineMax.");
            }
            request.setIdVenue(null); 
        }
        else if (request.getIdRole() == 3 || request.getIdRole() == 5) {
            if (request.getIdVenue() == null || request.getIdVenue() == 0) {
                throw new RuntimeException("Error: Este rol requiere ser asignado a una Sede.");
            }
            if (userRepository.existsByRole_IdRoleAndVenue_IdVenueAndStatus(request.getIdRole(), request.getIdVenue(), "Activo")) {
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

        Role rol = roleRepository.findById(request.getIdRole())
                .orElseThrow(() -> new RuntimeException("Rol no encontrado"));
        nuevoUsuario.setRole(rol);

        if (request.getIdVenue() != null && request.getIdVenue() > 0) {
            Venue sede = venueRepository.findById(request.getIdVenue())
                    .orElseThrow(() -> new RuntimeException("Sede no encontrada"));
            nuevoUsuario.setVenue(sede);
        }

        UserAccount guardado = userRepository.save(nuevoUsuario);
        return mapToResponseDTO(guardado);
    }

    @Override
    public UserResponseDTO updateUserRole(Integer idUser, UserRoleUpdateDTO request) {
        UserAccount usuarioExistente = userRepository.findById(idUser)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Role nuevoRol = roleRepository.findById(request.getIdRole())
                .orElseThrow(() -> new RuntimeException("Rol no encontrado"));
        
        if (request.getIdRole() == 2) {
            UserAccount gerenteActual = userRepository.findByRole_IdRoleAndStatus(2, "Activo").orElse(null);
            if (gerenteActual != null && !gerenteActual.getIdUser().equals(idUser)) {
                throw new RuntimeException("Error: Ya existe un Gerente General activo en CineMax.");
            }
            usuarioExistente.setVenue(null); 
        } 
        else if (request.getIdRole() == 3 || request.getIdRole() == 5) {
            if (request.getIdVenue() == null || request.getIdVenue() == 0) {
                throw new RuntimeException("Error: Este rol requiere ser asignado a una Sede.");
            }
            UserAccount gerenteSede = userRepository.findByRole_IdRoleAndVenue_IdVenueAndStatus(
                    request.getIdRole(), request.getIdVenue(), "Activo").orElse(null);
                    
            if (gerenteSede != null && !gerenteSede.getIdUser().equals(idUser)) {
                throw new RuntimeException("Error: La sede seleccionada ya tiene un gerente activo de este tipo.");
            }

            Venue nuevaSede = venueRepository.findById(request.getIdVenue())
                    .orElseThrow(() -> new RuntimeException("Sede no encontrada"));
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
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        usuario.setStatus("Inactivo");
        userRepository.save(usuario);
    }

    private UserResponseDTO mapToResponseDTO(UserAccount usuario) {
        UserResponseDTO response = new UserResponseDTO();
        response.setIdUser(usuario.getIdUser());
        response.setFirstName(usuario.getFirstName());
        response.setLastName(usuario.getLastName());
        response.setEmail(usuario.getEmail());
        response.setStatus(usuario.getStatus());

        if (usuario.getRole() != null) {
            response.setRoleName(usuario.getRole().getRoleName());
            response.setIdRole(usuario.getRole().getIdRole());
        }

        if (usuario.getVenue() != null) {
            response.setIdVenue(usuario.getVenue().getIdVenue()); // <-- AGREGADO
            response.setVenueName(usuario.getVenue().getNameVenue());
        }
        return response;
    }

    @Override
    public void activateUser(Integer idUser) {
        UserAccount user = userRepository.findById(idUser)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (user.getRole() != null) {
            Integer idRole = user.getRole().getIdRole();
            if (idRole == 2) {
                if (userRepository.existsByRole_IdRoleAndStatus(2, "Activo")) {
                    throw new RuntimeException("No se puede reactivar: Ya existe un Gerente General activo actualmente en el sistema.");
                }
            } 
            else if (idRole == 3 || idRole == 5) {
                if (user.getVenue() != null) {
                    Integer idVenue = user.getVenue().getIdVenue();
                    if (userRepository.existsByRole_IdRoleAndVenue_IdVenueAndStatus(idRole, idVenue, "Activo")) {
                        throw new RuntimeException("No se puede reactivar: La sede " + user.getVenue().getNameVenue() + " ya está ocupada por otro gerente activo.");
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
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

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
        
        // <-- AGREGADO: Esto es lo que hacía que fallara en Angular
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
    public UserResponseDTO updateProfile(UserUpdateDTO updateDTO, MultipartFile image, String email) {
        UserAccount user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (image != null && !image.isEmpty()) {
            try {
                String uploadedUrl = cloudinaryService.uploadImage(image);
                user.setImageUrl(uploadedUrl);
            } catch (Exception e) {
                throw new RuntimeException("Error al subir imagen a Cloudinary");
            }
        }

        if (updateDTO.getFirstName() != null) user.setFirstName(updateDTO.getFirstName());
        if (updateDTO.getLastName() != null) user.setLastName(updateDTO.getLastName());
        if (updateDTO.getPhone() != null) user.setPhoneNumber(updateDTO.getPhone());
        if (updateDTO.getDatebirth() != null) user.setBirthDate(updateDTO.getDatebirth());

        if (updateDTO.getNewPassword() != null && !updateDTO.getNewPassword().isEmpty()) {
             if (updateDTO.getOldPassword() == null || !passwordEncoder.matches(updateDTO.getOldPassword(), user.getPasswordHash())) {
                throw new RuntimeException("Contraseña actual incorrecta");
             }
             user.setPasswordHash(passwordEncoder.encode(updateDTO.getNewPassword()));
        }

        UserAccount updatedUser = userRepository.save(user);

        UserResponseDTO response = new UserResponseDTO();
        response.setIdUser(updatedUser.getIdUser());
        response.setFirstName(updatedUser.getFirstName());
        response.setLastName(updatedUser.getLastName());
        response.setEmail(updatedUser.getEmail());
        response.setStatus(updatedUser.getStatus());
        response.setDocumentNumber(updatedUser.getDocumentNumber());
        response.setPhone(updatedUser.getPhoneNumber());
        response.setDatebirth(updatedUser.getBirthDate());
        response.setImageUrl(updatedUser.getImageUrl());
        
        // <-- AGREGADO
        if (updatedUser.getRole() != null) {
            response.setIdRole(updatedUser.getRole().getIdRole());
            response.setRoleName(updatedUser.getRole().getRoleName());
        }
        if (updatedUser.getVenue() != null) {
            response.setIdVenue(updatedUser.getVenue().getIdVenue());
            response.setVenueName(updatedUser.getVenue().getNameVenue());
        }

        return response;
    }
}