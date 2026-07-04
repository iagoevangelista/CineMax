package com.cinemax.auth.service;

import com.cinemax.auth.dto.LoginRequestDTO;
import com.cinemax.auth.dto.LoginResponseDTO;

public interface AuthService {
    LoginResponseDTO login(LoginRequestDTO request);
}