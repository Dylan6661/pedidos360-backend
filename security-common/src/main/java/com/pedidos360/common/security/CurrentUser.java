package com.pedidos360.common.security;

import java.util.Arrays;
import java.util.List;

import org.springframework.security.oauth2.jwt.Jwt;

/** Lectura de los claims del usuario autenticado. */
public final class CurrentUser {

    public static final String ROLE_ADMIN = "Admin";
    public static final String ROLE_CLIENTE = "Cliente";

    private CurrentUser() {
    }

    /** Identificador único e inmutable del usuario en el tenant. */
    public static String oid(Jwt jwt) {
        String oid = jwt.getClaimAsString("oid");
        return oid != null ? oid : jwt.getSubject();
    }

    public static String name(Jwt jwt) {
        String name = jwt.getClaimAsString("name");
        return name != null ? name : email(jwt);
    }

    public static String email(Jwt jwt) {
        String email = jwt.getClaimAsString("email");
        if (email == null) {
            email = jwt.getClaimAsString("preferred_username");
        }
        if (email == null) {
            List<String> emails = jwt.getClaimAsStringList("emails");
            email = (emails != null && !emails.isEmpty()) ? emails.get(0) : null;
        }
        return email;
    }

    public static List<String> roles(Jwt jwt) {
        List<String> roles = jwt.getClaimAsStringList("roles");
        return roles != null ? roles : List.of();
    }

    public static List<String> scopes(Jwt jwt) {
        String scp = jwt.getClaimAsString("scp");
        if (scp == null || scp.isBlank()) {
            return List.of();
        }
        return Arrays.stream(scp.trim().split("\\s+")).toList();
    }

    public static boolean isAdmin(Jwt jwt) {
        return roles(jwt).contains(ROLE_ADMIN);
    }
}
