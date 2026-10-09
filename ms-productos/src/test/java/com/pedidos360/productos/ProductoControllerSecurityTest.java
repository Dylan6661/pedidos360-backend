package com.pedidos360.productos;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
class ProductoControllerSecurityTest {

    private static final String BODY = """
            {"nombre":"Café","descripcion":"Café americano","categoria":"Bebestible","precio":1990,"stock":10}
            """;

    @Autowired
    private MockMvc mvc;

    @Test
    void sinTokenResponde401() throws Exception {
        mvc.perform(get("/api/productos"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void conScopeReadPuedeListar() throws Exception {
        mvc.perform(get("/api/productos")
                        .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_Pedidos.Read"))))
                .andExpect(status().isOk());
    }

    @Test
    void sinScopeReadResponde403() throws Exception {
        mvc.perform(get("/api/productos").with(jwt().authorities()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void usuarioSinRolAdminNoPuedeCrear() throws Exception {
        mvc.perform(post("/api/productos")
                        .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_Pedidos.Write")))
                        .contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminPuedeCrear() throws Exception {
        mvc.perform(post("/api/productos")
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority("SCOPE_Pedidos.Write"),
                                new SimpleGrantedAuthority("ROLE_Admin")))
                        .contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Café"));
    }

    @Test
    void validaElCuerpoDeLaSolicitud() throws Exception {
        mvc.perform(post("/api/productos")
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority("SCOPE_Pedidos.Write"),
                                new SimpleGrantedAuthority("ROLE_Admin")))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"nombre\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void healthEsPublico() throws Exception {
        mvc.perform(get("/actuator/health")).andExpect(status().isOk());
    }
}
