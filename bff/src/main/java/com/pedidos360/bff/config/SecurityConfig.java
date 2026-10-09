package com.pedidos360.bff.config;

import static com.pedidos360.common.security.Pedidos360Security.canRead;
import static com.pedidos360.common.security.Pedidos360Security.canWrite;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

import com.pedidos360.common.security.Pedidos360Security;

/**
 * El BFF valida el JWT igual que el API Gateway (firma, vigencia, issuer, audience)
 * antes de orquestar llamadas a los microservicios.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, Pedidos360Security security) throws Exception {
        security.applyDefaults(http)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/bff/**").access(canRead())
                        .requestMatchers(HttpMethod.POST, "/api/bff/pedidos").access(canWrite())
                        .anyRequest().denyAll());
        return http.build();
    }
}
