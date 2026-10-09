package com.pedidos360.productos.config;

import static com.pedidos360.common.security.Pedidos360Security.adminWrite;
import static com.pedidos360.common.security.Pedidos360Security.canRead;

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
                        // Cualquier usuario autenticado con scope Pedidos.Read puede ver el catálogo
                        .requestMatchers(HttpMethod.GET, "/api/productos/**").access(canRead())
                        // Crear, editar y eliminar productos: solo rol Admin con scope Pedidos.Write
                        .requestMatchers("/api/productos/**").access(adminWrite())
                        .anyRequest().denyAll());
        return http.build();
    }
}
