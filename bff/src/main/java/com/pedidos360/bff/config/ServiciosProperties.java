package com.pedidos360.bff.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** URLs internas de los microservicios que el BFF orquesta. */
@ConfigurationProperties(prefix = "servicios")
public record ServiciosProperties(String productosUrl, String clientesUrl, String pedidosUrl) {
}
