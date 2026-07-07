package com.cinemax.usuariosservice.service.email;

public interface EmailService {
    void sendPasswordResetEmail(String toEmail, String resetToken);
}