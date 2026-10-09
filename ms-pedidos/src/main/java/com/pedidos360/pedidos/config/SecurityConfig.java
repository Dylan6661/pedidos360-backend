package com.pedidos360.pedidos.config;

import static com.pedidos360.common.security.Pedidos360Security.adminWrite;
import static com.pedidos360.common.security.Pedidos360Security.canRead;
import static com.pedidos360.common.security.Pedidos360Security.canWrite;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

import com.pedidos360.common.security.Pedidos360Security;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, Pedidos360Security security) throws Exception {
        security.applyDefaults(http)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                        // Cambiar el estado de un pedido: solo Admin
                        .requestMatchers(HttpMethod.PATCH, "/api/pedidos/*/estado").access(adminWrite())
                        // Consultar pedidos: el servicio filtra los propios (o todos si es Admin)
                        .requestMatchers(HttpMethod.GET, "/api/pedidos/**").access(canRead())
                        // Crear y cancelar pedidos propios
                        .requestMatchers(HttpMethod.POST, "/api/pedidos", "/api/pedidos/*/cancelar").access(canWrite())
                        .requestMatchers(HttpMethod.DELETE, "/api/pedidos/**").access(adminWrite())
                        .anyRequest().denyAll());
        return http.build();
    }
}
