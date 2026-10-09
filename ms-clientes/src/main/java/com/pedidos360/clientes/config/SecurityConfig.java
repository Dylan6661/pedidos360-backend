package com.pedidos360.clientes.config;

import static com.pedidos360.common.security.Pedidos360Security.adminRead;
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
                        // Perfil propio: cualquier usuario autenticado
                        .requestMatchers(HttpMethod.GET, "/api/clientes/me").access(canRead())
                        .requestMatchers(HttpMethod.PUT, "/api/clientes/me").access(canWrite())
                        // Administración de clientes: solo rol Admin
                        .requestMatchers(HttpMethod.GET, "/api/clientes/**").access(adminRead())
                        .requestMatchers("/api/clientes/**").access(adminWrite())
                        .anyRequest().denyAll());
        return http.build();
    }
}
