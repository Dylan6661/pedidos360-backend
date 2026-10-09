package com.pedidos360.common.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

class JwtValidationTest {

    private Jwt token(List<String> aud) {
        return Jwt.withTokenValue("t")
                .header("alg", "RS256")
                .issuer("https://tenant.ciamlogin.com/tenant/v2.0")
                .audience(aud)
                .subject("sub-1")
                .claim("oid", "oid-123")
                .claim("scp", "Pedidos.Read Pedidos.Write")
                .claim("roles", List.of("Admin"))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300))
                .build();
    }

    @Test
    void aceptaAudienceCorrecta() {
        AudienceValidator validator = new AudienceValidator(List.of("api-client-id"));
        assertThat(validator.validate(token(List.of("api-client-id"))).hasErrors()).isFalse();
    }

    @Test
    void rechazaAudienceIncorrecta() {
        AudienceValidator validator = new AudienceValidator(List.of("api-client-id"));
        assertThat(validator.validate(token(List.of("otra-api"))).hasErrors()).isTrue();
    }

    @Test
    void mapeaScopesYRolesComoAuthorities() {
        List<String> authorities = EntraJwtAuthenticationConverter.extractAuthorities(token(List.of("x")))
                .stream().map(GrantedAuthority::getAuthority).toList();
        assertThat(authorities).containsExactlyInAnyOrder(
                "SCOPE_Pedidos.Read", "SCOPE_Pedidos.Write", "ROLE_Admin");
    }

    @Test
    void usaOidComoIdentificadorDelUsuario() {
        Jwt jwt = token(List.of("x"));
        assertThat(CurrentUser.oid(jwt)).isEqualTo("oid-123");
        assertThat(CurrentUser.isAdmin(jwt)).isTrue();
    }
}
