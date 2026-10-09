package com.pedidos360.common.security;

import org.springframework.security.authorization.AuthorityAuthorizationManager;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.AuthorizationManagers;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

/**
 * Configuración común de seguridad que aplican todos los microservicios:
 * API REST sin estado, validación de JWT como resource server y errores 401/403 en JSON.
 * Cada servicio solo agrega sus reglas de autorización por ruta.
 */
public class Pedidos360Security {

    public static final String SCOPE_READ = "SCOPE_Pedidos.Read";
    public static final String SCOPE_WRITE = "SCOPE_Pedidos.Write";

    private final EntraJwtAuthenticationConverter converter;
    private final JsonSecurityErrorHandler errorHandler;
    private final boolean corsEnabled;

    public Pedidos360Security(EntraJwtAuthenticationConverter converter,
                              JsonSecurityErrorHandler errorHandler,
                              boolean corsEnabled) {
        this.converter = converter;
        this.errorHandler = errorHandler;
        this.corsEnabled = corsEnabled;
    }

    public HttpSecurity applyDefaults(HttpSecurity http) throws Exception {
        if (corsEnabled) {
            http.cors(Customizer.withDefaults());
        } else {
            http.cors(cors -> cors.disable());
        }
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(b -> b.disable())
                .formLogin(f -> f.disable())
                .oauth2ResourceServer(rs -> rs
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(converter))
                        .authenticationEntryPoint(errorHandler)
                        .accessDeniedHandler(errorHandler))
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(errorHandler)
                        .accessDeniedHandler(errorHandler));
    }

    /** Lectura: requiere el scope Pedidos.Read. */
    public static AuthorizationManager<RequestAuthorizationContext> canRead() {
        return AuthorityAuthorizationManager.hasAuthority(SCOPE_READ);
    }

    /** Escritura del propio usuario: requiere el scope Pedidos.Write. */
    public static AuthorizationManager<RequestAuthorizationContext> canWrite() {
        return AuthorityAuthorizationManager.hasAuthority(SCOPE_WRITE);
    }

    /** Lectura administrativa: scope Pedidos.Read + rol Admin. */
    public static AuthorizationManager<RequestAuthorizationContext> adminRead() {
        return AuthorizationManagers.allOf(
                AuthorityAuthorizationManager.<RequestAuthorizationContext>hasAuthority(SCOPE_READ),
                AuthorityAuthorizationManager.<RequestAuthorizationContext>hasRole(CurrentUser.ROLE_ADMIN));
    }

    /** Escritura administrativa: scope Pedidos.Write + rol Admin. */
    public static AuthorizationManager<RequestAuthorizationContext> adminWrite() {
        return AuthorizationManagers.allOf(
                AuthorityAuthorizationManager.<RequestAuthorizationContext>hasAuthority(SCOPE_WRITE),
                AuthorityAuthorizationManager.<RequestAuthorizationContext>hasRole(CurrentUser.ROLE_ADMIN));
    }
}
