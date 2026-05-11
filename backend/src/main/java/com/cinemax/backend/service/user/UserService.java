package com.cinemax.backend.service.user;

import com.cinemax.backend.model.dto.user.UserResponseDTO;
import com.cinemax.backend.model.dto.user.UserRoleUpdateDTO;

import java.util.List;

public interface UserService {
    List<UserResponseDTO> getAllUsers();
    UserResponseDTO updateUserRole(Integer idUser, UserRoleUpdateDTO request);
}