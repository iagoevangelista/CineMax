package com.cinemax.usuariosservice.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_account")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class UserAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_user")
    private Integer idUser;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_role", nullable = false)
    private Role role;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_doc_type", nullable = false)
    private DocumentType documentType;

    // Desacoplado de Sucursales-Service
    @Column(name = "id_venue")
    private Integer idVenue;

    @Column(name = "document_number", length = 20, nullable = false)
    private String documentNumber;

    @Column(name = "first_name", length = 100, nullable = false)
    private String firstName;

    @Column(name = "last_name", length = 100, nullable = false)
    private String lastName;

    @Column(name = "email", length = 150, nullable = false, unique = true)
    private String email;

    @Column(name = "password", length = 255, nullable = false)
    private String password;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "status", length = 20, nullable = false)
    private String status;

    @Column(name = "reset_password_token", length = 255)
    private String resetPasswordToken;

    @Column(name = "token_expiration_date")
    private LocalDateTime tokenExpirationDate;

    @Column(name = "datebirth")
    private java.time.LocalDate datebirth;

    @Column(name = "image_url", length = 255)
    private String imageUrl;
}