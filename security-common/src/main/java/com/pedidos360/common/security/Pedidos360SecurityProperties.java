package com.pedidos360.common.security;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Parámetros del IDaaS (Microsoft Entra External ID) usados para validar los JWT.
 *
 * <pre>
 * pedidos360:
 *   security:
 *     issuer-uri: https://{tenant-id}.ciamlogin.com/{tenant-id}/v2.0
 *     jwk-set-uri: https://{subdominio}.ciamlogin.com/{tenant-id}/discovery/v2.0/keys
 *     audiences: {client-id-de-la-api}
 * </pre>
 */
@ConfigurationProperties(prefix = "pedidos360.security")
public class Pedidos360SecurityProperties {

    /** Valor exacto que debe traer el claim "iss" del token. */
    private String issuerUri;

    /** URL de las llaves públicas (JWKS) para verificar la firma del token. */
    private String jwkSetUri;

    /** Valores aceptados para el claim "aud" (client ID o App ID URI de la API). */
    private List<String> audiences = new ArrayList<>();

    /** Orígenes permitidos para CORS cuando el frontend llama directo al servicio (desarrollo local). */
    private List<String> corsAllowedOrigins = new ArrayList<>();

    public String getIssuerUri() { return issuerUri; }
    public void setIssuerUri(String issuerUri) { this.issuerUri = issuerUri; }

    public String getJwkSetUri() { return jwkSetUri; }
    public void setJwkSetUri(String jwkSetUri) { this.jwkSetUri = jwkSetUri; }

    public List<String> getAudiences() { return audiences; }
    public void setAudiences(List<String> audiences) { this.audiences = audiences; }

    public List<String> getCorsAllowedOrigins() { return corsAllowedOrigins; }
    public void setCorsAllowedOrigins(List<String> corsAllowedOrigins) { this.corsAllowedOrigins = corsAllowedOrigins; }
}
