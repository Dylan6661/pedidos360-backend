package com.pedidos360.bff.config;

import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    /**
     * Reenvía el mismo JWT del usuario a los microservicios (token propagation),
     * de modo que cada servicio vuelve a validar el token y aplica sus propias reglas de rol.
     */
    @Bean
    ClientHttpRequestInterceptor bearerTokenPropagation() {
        return (request, body, execution) -> {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth instanceof JwtAuthenticationToken jwtAuth) {
                request.getHeaders().set(HttpHeaders.AUTHORIZATION, "Bearer " + jwtAuth.getToken().getTokenValue());
            }
            return execution.execute(request, body);
        };
    }

    private RestClient build(String baseUrl, ClientHttpRequestInterceptor interceptor) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(3));
        factory.setReadTimeout(Duration.ofSeconds(10));
        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .requestInterceptor(interceptor)
                .build();
    }

    @Bean
    RestClient productosRestClient(ServiciosProperties props, ClientHttpRequestInterceptor bearerTokenPropagation) {
        return build(props.productosUrl(), bearerTokenPropagation);
    }

    @Bean
    RestClient clientesRestClient(ServiciosProperties props, ClientHttpRequestInterceptor bearerTokenPropagation) {
        return build(props.clientesUrl(), bearerTokenPropagation);
    }

    @Bean
    RestClient pedidosRestClient(ServiciosProperties props, ClientHttpRequestInterceptor bearerTokenPropagation) {
        return build(props.pedidosUrl(), bearerTokenPropagation);
    }
}
