package com.cinemax.backend.service.user;

import com.cinemax.backend.model.entity.Venue;
import com.cinemax.backend.model.dto.user.UserCreateDTO;
import com.cinemax.backend.model.dto.user.UserResponseDTO;
import com.cinemax.backend.model.dto.user.UserRoleUpdateDTO;
import com.cinemax.backend.model.entity.Role;
import com.cinemax.backend.model.entity.UserAccount;
import com.cinemax.backend.repository.RoleRepository;
import com.cinemax.backend.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.cinemax.backend.repository.VenueRepository;


import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserAccountRepository userRepository;
    private final RoleRepository roleRepository;
    private final VenueRepository venueRepository; // Inyectar
    private final PasswordEncoder passwordEncoder; // Inyectar

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

            // NUEVO: Pasamos el estado a Angular
            dto.setStatus(usuario.getStatus());

            // Navegamos para sacar el Rol
            if (usuario.getRole() != null) {
                dto.setRoleName(usuario.getRole().getRoleName());
                dto.setIdRole(usuario.getRole().getIdRole());
            }

            // NUEVO: Navegamos para sacar el nombre de la Sede (si tiene)
            if (usuario.getVenue() != null) {
                dto.setVenueName(usuario.getVenue().getNameVenue());
            }

            listaResponse.add(dto);
        }
        return listaResponse;
    }

    @Override
    public UserResponseDTO createUser(UserCreateDTO request) {
        // 1. REGLA DE NEGOCIO: ¿Ya hay alguien con ese rol en esa sede?
        // (Opcional: puedes implementar una consulta personalizada en el repo)

        UserAccount nuevoUsuario = new UserAccount();
        nuevoUsuario.setFirstName(request.getFirstName());
        nuevoUsuario.setLastName(request.getLastName());
        nuevoUsuario.setEmail(request.getEmail());
        nuevoUsuario.setStatus("Activo");

        // 2. ENCRIPTAR CONTRASEÑA (Vital para que pueda hacer login luego)
        nuevoUsuario.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        // 3. ASIGNAR ROL
        Role rol = roleRepository.findById(request.getIdRole())
                .orElseThrow(() -> new RuntimeException("Rol no encontrado"));
        nuevoUsuario.setRole(rol);

        // 4. ASIGNAR SEDE (Si no es Admin)
        if (request.getIdVenue() != null && request.getIdVenue() > 0) {
            Venue sede = venueRepository.findById(request.getIdVenue())
                    .orElseThrow(() -> new RuntimeException("Sede no encontrada"));
            nuevoUsuario.setVenue(sede);
        }

        UserAccount guardado = userRepository.save(nuevoUsuario);
        return mapToResponseDTO(guardado); // Método auxiliar para convertir a ResponseDTO
    }

    @Override
    public UserResponseDTO updateUserRole(Integer idUser, UserRoleUpdateDTO request) {
        UserAccount usuarioExistente = userRepository.findById(idUser)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Role nuevoRol = roleRepository.findById(request.getIdRole())
                .orElseThrow(() -> new RuntimeException("Rol no encontrado"));

        usuarioExistente.setRole(nuevoRol);
        UserAccount usuarioActualizado = userRepository.save(usuarioExistente);

        UserResponseDTO response = new UserResponseDTO();
        response.setIdUser(usuarioActualizado.getIdUser());
        response.setFirstName(usuarioActualizado.getFirstName());
        response.setLastName(usuarioActualizado.getLastName());
        response.setEmail(usuarioActualizado.getEmail());

        // NUEVO: También actualizamos aquí para la respuesta de edición
        response.setStatus(usuarioActualizado.getStatus());

        response.setRoleName(nuevoRol.getRoleName());
        response.setIdRole(nuevoRol.getIdRole());

        if (usuarioActualizado.getVenue() != null) {
            response.setVenueName(usuarioActualizado.getVenue().getNameVenue());
        }

        return response;
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
            response.setVenueName(usuario.getVenue().getNameVenue());
        }
        return response;
    }
}