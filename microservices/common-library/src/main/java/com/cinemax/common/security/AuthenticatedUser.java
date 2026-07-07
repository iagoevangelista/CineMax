package com.cinemax.common.security;

import lombok.Getter;

import java.security.Principal;
import java.util.List;

@Getter
public class AuthenticatedUser implements Principal {

    private final String email;
    private final String role;
    private final Integer idVenue;
    private final List<String> permissions;

    public AuthenticatedUser(String email, String role, Integer idVenue, List<String> permissions) {
        this.email = email;
        this.role = role;
        this.idVenue = idVenue;
        this.permissions = permissions;
    }

    @Override
    public String getName() {
        return email;
    }
}