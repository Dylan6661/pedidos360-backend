package com.pedidos360.common.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/**
 * Convierte un JWT de Entra en una autenticación de Spring Security:
 * <ul>
 *   <li>claim "scp" (scopes delegados, separados por espacio) → SCOPE_xxx</li>
 *   <li>claim "roles" (app roles asignados al usuario) → ROLE_xxx</li>
 *   <li>el nombre del principal es el claim "oid" (id único del usuario en el tenant)</li>
 * </ul>
 */
public class EntraJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        return new JwtAuthenticationToken(jwt, extractAuthorities(jwt), CurrentUser.oid(jwt));
    }

    static Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        for (String scope : CurrentUser.scopes(jwt)) {
            authorities.add(new SimpleGrantedAuthority("SCOPE_" + scope));
        }
        for (String role : CurrentUser.roles(jwt)) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
        }
        return authorities;
    }
}
