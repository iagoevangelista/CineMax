package com.cinemax.backend.service.email; // Asegúrate de que el paquete sea el correcto

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    // Inyectamos el correo desde el application.properties para no tenerlo "quemado" en el código
    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendPasswordResetEmail(String toEmail, String resetToken) {
        // Por ahora apuntamos al localhost de Angular, luego en producción esto cambia a tu dominio real
        String resetLink = "http://localhost:4200/reset-password?token=" + resetToken;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("CineMax - Recuperación de Contraseña");
        message.setText("Hola,\n\n"
                + "Hemos recibido una solicitud para restablecer la contraseña de tu cuenta corporativa en CineMax.\n\n"
                + "Haz clic en el siguiente enlace para crear una nueva contraseña:\n"
                + resetLink + "\n\n"
                + "Por motivos de seguridad, este enlace expirará en 15 minutos.\n"
                + "Si no solicitaste este cambio, simplemente ignora este mensaje.\n\n"
                + "Atentamente,\n"
                + "El equipo de TI de CineMax");

        mailSender.send(message);
    }

    public void sendWelcomeEmail(String toEmail, String firstName) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("¡Bienvenido a CineMax, " + firstName + "!");
        message.setText("Hola " + firstName + ",\n\n"
                + "Queremos darte una cálida bienvenida a la familia CineMax.\n\n"
                + "Tu cuenta ha sido creada exitosamente. Ahora podrás disfrutar de la mejor experiencia "
                + "comprando tus entradas y combos favoritos desde la comodidad de tu hogar.\n\n"
                + "¡Nos vemos en el cine!\n\n"
                + "Atentamente,\n"
                + "El equipo de CineMax");

        mailSender.send(message);
    }
}