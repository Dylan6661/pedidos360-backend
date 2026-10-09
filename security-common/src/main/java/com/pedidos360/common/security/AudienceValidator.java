package com.pedidos360.common.security;

import java.util.Collection;
import java.util.List;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

/** Rechaza los tokens cuyo claim "aud" no corresponde a la API de Pedidos360. */
public class AudienceValidator implements OAuth2TokenValidator<Jwt> {

    private final Collection<String> allowedAudiences;

    public AudienceValidator(Collection<String> allowedAudiences) {
        if (allowedAudiences == null || allowedAudiences.isEmpty()) {
            throw new IllegalArgumentException("Debe configurar al menos una audience (pedidos360.security.audiences)");
        }
        this.allowedAudiences = List.copyOf(allowedAudiences);
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {
        List<String> tokenAudiences = jwt.getAudience();
        if (tokenAudiences != null && tokenAudiences.stream().anyMatch(allowedAudiences::contains)) {
            return OAuth2TokenValidatorResult.success();
        }
        OAuth2Error error = new OAuth2Error(OAuth2ErrorCodes.INVALID_TOKEN,
                "El token no fue emitido para esta API (audience inválida)", null);
        return OAuth2TokenValidatorResult.failure(error);
    }
}
