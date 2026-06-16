package com.cinemax.backend.util;

public final class RoleConstants {

    private RoleConstants() {}

    public static final String ADMIN                  = "ADMIN";
    public static final String GERENTE_GENERAL        = "GERENTE_GENERAL";
    public static final String GERENTE_MARKETING      = "GERENTE_DE_MARKETING";
    public static final String GERENTE_OPERACIONES    = "GERENTE_DE_OPERACIONES";
    public static final String CLIENTE                = "CLIENTE";

    // Prefijo que agrega Spring Security al nombre del rol 
    public static final String SPRING_PREFIX          = "ROLE_";
}
