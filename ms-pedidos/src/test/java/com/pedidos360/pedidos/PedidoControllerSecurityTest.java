package com.pedidos360.pedidos;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
import org.springframework.test.web.servlet.MockMvc;

import com.jayway.jsonpath.JsonPath;

@SpringBootTest
@AutoConfigureMockMvc
class PedidoControllerSecurityTest {

    private static final String PEDIDO = """
            {"clienteId":1,"direccionEntrega":"Av. Test 123",
             "items":[{"productoId":1,"nombreProducto":"Pizza","cantidad":2,"precioUnitario":9990}]}
            """;

    @Autowired
    private MockMvc mvc;

    private JwtRequestPostProcessor usuario(String oid, String... authorities) {
        SimpleGrantedAuthority[] auths = new SimpleGrantedAuthority[authorities.length];
        for (int i = 0; i < authorities.length; i++) {
            auths[i] = new SimpleGrantedAuthority(authorities[i]);
        }
        // Igual que en un token real: los roles vienen en el claim "roles"
        java.util.List<String> roles = java.util.Arrays.stream(authorities)
                .filter(a -> a.startsWith("ROLE_")).map(a -> a.substring(5)).toList();
        return jwt().jwt(j -> j.claim("oid", oid).claim("roles", roles)).authorities(auths);
    }

    @Test
    void sinTokenResponde401() throws Exception {
        mvc.perform(get("/api/pedidos")).andExpect(status().isUnauthorized());
    }

    @Test
    void creaPedidoYCalculaTotal() throws Exception {
        mvc.perform(post("/api/pedidos")
                        .with(usuario("oid-1", "SCOPE_Pedidos.Write"))
                        .contentType(MediaType.APPLICATION_JSON).content(PEDIDO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.total").value(19980))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.usuarioOid").value("oid-1"));
    }

    @Test
    void otroUsuarioNoPuedeVerElPedido() throws Exception {
        String json = mvc.perform(post("/api/pedidos")
                        .with(usuario("oid-dueno", "SCOPE_Pedidos.Write"))
                        .contentType(MediaType.APPLICATION_JSON).content(PEDIDO))
                .andReturn().getResponse().getContentAsString();
        Integer id = JsonPath.read(json, "$.id");

        mvc.perform(get("/api/pedidos/" + id).with(usuario("oid-otro", "SCOPE_Pedidos.Read")))
                .andExpect(status().isForbidden());

        mvc.perform(get("/api/pedidos/" + id).with(usuario("oid-dueno", "SCOPE_Pedidos.Read")))
                .andExpect(status().isOk());

        mvc.perform(get("/api/pedidos/" + id).with(usuario("oid-admin", "SCOPE_Pedidos.Read", "ROLE_Admin")))
                .andExpect(status().isOk());
    }

    @Test
    void cambiarEstadoRequiereAdmin() throws Exception {
        String json = mvc.perform(post("/api/pedidos")
                        .with(usuario("oid-2", "SCOPE_Pedidos.Write"))
                        .contentType(MediaType.APPLICATION_JSON).content(PEDIDO))
                .andReturn().getResponse().getContentAsString();
        Integer id = JsonPath.read(json, "$.id");
        String body = "{\"estado\":\"CONFIRMADO\"}";

        mvc.perform(patch("/api/pedidos/" + id + "/estado")
                        .with(usuario("oid-2", "SCOPE_Pedidos.Write"))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());

        mvc.perform(patch("/api/pedidos/" + id + "/estado")
                        .with(usuario("oid-admin", "SCOPE_Pedidos.Write", "ROLE_Admin"))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CONFIRMADO"));
    }

    @Test
    void pedidoSinItemsResponde400() throws Exception {
        mvc.perform(post("/api/pedidos")
                        .with(usuario("oid-3", "SCOPE_Pedidos.Write"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"clienteId\":1,\"items\":[]}"))
                .andExpect(status().isBadRequest());
    }
}
