package com.pedidos360.clientes;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ClienteControllerSecurityTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void sinTokenResponde401() throws Exception {
        mvc.perform(get("/api/clientes/me")).andExpect(status().isUnauthorized());
    }

    @Test
    void usuarioGuardaYLeeSuPerfil() throws Exception {
        String body = """
                {"nombre":"Ana Pérez","telefono":"+56911112222","direccion":"Av. Siempre Viva 742"}
                """;
        mvc.perform(put("/api/clientes/me")
                        .with(jwt().jwt(j -> j.claim("oid", "oid-ana").claim("email", "ana@test.cl"))
                                .authorities(new SimpleGrantedAuthority("SCOPE_Pedidos.Write")))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("ana@test.cl"));

        mvc.perform(get("/api/clientes/me")
                        .with(jwt().jwt(j -> j.claim("oid", "oid-ana"))
                                .authorities(new SimpleGrantedAuthority("SCOPE_Pedidos.Read"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Ana Pérez"));
    }

    @Test
    void perfilInexistenteResponde404() throws Exception {
        mvc.perform(get("/api/clientes/me")
                        .with(jwt().jwt(j -> j.claim("oid", "oid-nuevo"))
                                .authorities(new SimpleGrantedAuthority("SCOPE_Pedidos.Read"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void listarTodosRequiereRolAdmin() throws Exception {
        mvc.perform(get("/api/clientes")
                        .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_Pedidos.Read"))))
                .andExpect(status().isForbidden());

        mvc.perform(get("/api/clientes")
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority("SCOPE_Pedidos.Read"),
                                new SimpleGrantedAuthority("ROLE_Admin"))))
                .andExpect(status().isOk());
    }
}
