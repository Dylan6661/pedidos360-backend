package com.pedidos360.bff;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.pedidos360.bff.client.ClientesClient;
import com.pedidos360.bff.client.PedidosClient;
import com.pedidos360.bff.client.ProductosClient;
import com.pedidos360.bff.dto.ClienteDto;
import com.pedidos360.bff.dto.NuevoPedidoDto;
import com.pedidos360.bff.dto.PedidoDto;
import com.pedidos360.bff.dto.ProductoDto;

@SpringBootTest
@AutoConfigureMockMvc
class BffControllerTest {

    private static final String PEDIDO = """
            {"items":[{"productoId":1,"cantidad":2}]}
            """;

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private ProductosClient productos;
    @MockitoBean
    private ClientesClient clientes;
    @MockitoBean
    private PedidosClient pedidos;

    private final ClienteDto cliente = new ClienteDto(7L, "oid-1", "Ana", "ana@test.cl", null, "Av. Test 1");
    private final ProductoDto pizza = new ProductoDto(1L, "Pizza", "", "Comida", new BigDecimal("9990"), 5);

    @Test
    void sinTokenResponde401() throws Exception {
        mvc.perform(get("/api/bff/resumen")).andExpect(status().isUnauthorized());
    }

    @Test
    void crearPedidoSinScopeWriteResponde403() throws Exception {
        mvc.perform(post("/api/bff/pedidos")
                        .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_Pedidos.Read")))
                        .contentType(MediaType.APPLICATION_JSON).content(PEDIDO))
                .andExpect(status().isForbidden());
    }

    @Test
    void creaPedidoConPreciosDelCatalogo() throws Exception {
        when(clientes.miPerfil()).thenReturn(Optional.of(cliente));
        when(productos.obtener(1L)).thenReturn(pizza);
        when(pedidos.crear(any())).thenReturn(new PedidoDto(10L, 7L, "oid-1", "PENDIENTE", "Av. Test 1",
                new BigDecimal("19980"), Instant.now(),
                List.of(new PedidoDto.Item(1L, "Pizza", 2, new BigDecimal("9990"), new BigDecimal("19980")))));

        mvc.perform(post("/api/bff/pedidos")
                        .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_Pedidos.Write")))
                        .contentType(MediaType.APPLICATION_JSON).content(PEDIDO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.pedido.id").value(10))
                .andExpect(jsonPath("$.cliente.nombre").value("Ana"));

        ArgumentCaptor<NuevoPedidoDto> captor = ArgumentCaptor.forClass(NuevoPedidoDto.class);
        verify(pedidos).crear(captor.capture());
        assertThat(captor.getValue().clienteId()).isEqualTo(7L);
        assertThat(captor.getValue().items().get(0).precioUnitario()).isEqualByComparingTo("9990");
    }

    @Test
    void stockInsuficienteResponde409() throws Exception {
        when(clientes.miPerfil()).thenReturn(Optional.of(cliente));
        when(productos.obtener(1L)).thenReturn(new ProductoDto(1L, "Pizza", "", "Comida", new BigDecimal("9990"), 1));

        mvc.perform(post("/api/bff/pedidos")
                        .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_Pedidos.Write")))
                        .contentType(MediaType.APPLICATION_JSON).content(PEDIDO))
                .andExpect(status().isConflict());
        verify(pedidos, never()).crear(any());
    }

    @Test
    void sinPerfilDeClienteResponde400() throws Exception {
        when(clientes.miPerfil()).thenReturn(Optional.empty());

        mvc.perform(post("/api/bff/pedidos")
                        .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_Pedidos.Write")))
                        .contentType(MediaType.APPLICATION_JSON).content(PEDIDO))
                .andExpect(status().isBadRequest());
    }
}
