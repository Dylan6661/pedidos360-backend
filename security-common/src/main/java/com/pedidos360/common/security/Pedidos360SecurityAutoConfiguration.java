package com.pedidos360.common.security;

import java.util.List;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.util.StringUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pedidos360.common.error.GlobalExceptionHandler;

/**
 * Se carga automáticamente en cada microservicio que dependa de security-common.
 * Define cómo se valida el JWT emitido por el IDaaS:
 * firma (JWKS) + vigencia (exp/nbf) + issuer + audience.
 */
@AutoConfiguration
@EnableConfigurationProperties(Pedidos360SecurityProperties.class)
public class Pedidos360SecurityAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public JwtDecoder jwtDecoder(Pedidos360SecurityProperties props) {
        if (!StringUtils.hasText(props.getJwkSetUri()) || !StringUtils.hasText(props.getIssuerUri())) {
            throw new IllegalStateException(
                    "Configure pedidos360.security.issuer-uri y pedidos360.security.jwk-set-uri (variables IDAAS_ISSUER y IDAAS_JWKS_URI)");
        }
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(props.getJwkSetUri()).build();
        OAuth2TokenValidator<Jwt> validator = new DelegatingOAuth2TokenValidator<>(
                // Incluye validación de exp / nbf (vigencia) y del claim iss
                JwtValidators.createDefaultWithIssuer(props.getIssuerUri()),
                new AudienceValidator(props.getAudiences()));
        decoder.setJwtValidator(validator);
        return decoder;
    }

    @Bean
    @ConditionalOnMissingBean
    public EntraJwtAuthenticationConverter entraJwtAuthenticationConverter() {
        return new EntraJwtAuthenticationConverter();
    }

    @Bean
    @ConditionalOnMissingBean
    public JsonSecurityErrorHandler jsonSecurityErrorHandler(ObjectMapper objectMapper) {
        return new JsonSecurityErrorHandler(objectMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public Pedidos360Security pedidos360Security(EntraJwtAuthenticationConverter converter,
                                                 JsonSecurityErrorHandler errorHandler,
                                                 Pedidos360SecurityProperties props) {
        return new Pedidos360Security(converter, errorHandler, !props.getCorsAllowedOrigins().isEmpty());
    }

    /** Solo para desarrollo local (en la nube el CORS lo resuelve el API Gateway). */
    @Bean
    @ConditionalOnMissingBean(name = "corsConfigurationSource")
    public CorsConfigurationSource corsConfigurationSource(Pedidos360SecurityProperties props) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(props.getCorsAllowedOrigins());
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    @ConditionalOnMissingBean
    public GlobalExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler();
    }
}
