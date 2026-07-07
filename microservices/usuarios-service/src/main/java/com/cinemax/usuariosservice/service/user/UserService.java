package com.cinemax.usuariosservice.service.user;

import com.cinemax.usuariosservice.model.dto.auth.ValidateCredentialsRequestDTO;
import com.cinemax.usuariosservice.model.dto.auth.ValidateCredentialsResponseDTO;
import com.cinemax.usuariosservice.model.dto.user.*;
import com.cinemax.usuariosservice.model.dto.venue.VenueDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserService {
    List<UserResponseDTO> getAllUsers();
    UserResponseDTO createUser(UserCreateDTO request);
    
    UserResponseDTO updateUserRole(Integer idUser, UserRoleUpdateDTO request);
    void deleteUser(Integer idUser);
    void activateUser(Integer idUser);
    void registerClient(com.cinemax.usuariosservice.model.dto.auth.UserRegisterDTO request);

    ValidateCredentialsResponseDTO validateCredentials(ValidateCredentialsRequestDTO request);

    void requestPasswordReset(String email);
    void resetPassword(String token, String newPassword);
    boolean validateResetToken(String token);

    UserResponseDTO getMyProfile(String email);
    UserResponseDTO updateProfile(UserUpdateDTO updateDTO, MultipartFile image, String email);
    void deleteMyAccount(String email);

    List<VenueDTO> getAvailableVenuesForRole(Integer idRole);
}