package com.cinemax.backend.service.auth;

import com.cinemax.backend.model.dto.auth.AuthResponseDTO;
import com.cinemax.backend.model.dto.auth.AuthRequestDTO;
import com.cinemax.backend.model.dto.auth.RegisterRequestDTO;

public interface AuthService {
    AuthResponseDTO login(AuthRequestDTO request);
    AuthResponseDTO register(RegisterRequestDTO request);
    void requestPasswordReset(String email);
    void resetPassword(String token, String newPassword);
}