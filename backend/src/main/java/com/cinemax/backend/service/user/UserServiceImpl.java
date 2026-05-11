package com.cinemax.backend.service.user;

import com.cinemax.backend.model.dto.user.UserResponseDTO;
import com.cinemax.backend.model.dto.user.UserRoleUpdateDTO;
import com.cinemax.backend.model.entity.Role;
import com.cinemax.backend.model.entity.UserAccount;
import com.cinemax.backend.repository.RoleRepository;
import com.cinemax.backend.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserAccountRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    public List<UserResponseDTO> getAllUsers() {
        List<UserAccount> usuarios = userRepository.findAll();
        List<UserResponseDTO> listaResponse = new ArrayList<>();

        for (UserAccount usuario : usuarios) {
            UserResponseDTO dto = new UserResponseDTO();
            dto.setIdUser(usuario.getIdUser());
            dto.setFirstName(usuario.getFirstName());
            dto.setLastName(usuario.getLastName());
            dto.setEmail(usuario.getEmail());
            
            // Navegamos para sacar el Rol
            if (usuario.getRole() != null) {
                dto.setRoleName(usuario.getRole().getRoleName());
                dto.setIdRole(usuario.getRole().getIdRole());
            }
            
            listaResponse.add(dto);
        }
        return listaResponse;
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
        
        response.setRoleName(nuevoRol.getRoleName());
        response.setIdRole(nuevoRol.getIdRole());

        return response;
    }
}