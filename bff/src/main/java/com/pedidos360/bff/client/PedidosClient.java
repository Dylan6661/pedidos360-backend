package com.pedidos360.bff.client;

import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.pedidos360.bff.dto.NuevoPedidoDto;
import com.pedidos360.bff.dto.PedidoDto;

@Component
public class PedidosClient {

    private final RestClient client;

    public PedidosClient(@Qualifier("pedidosRestClient") RestClient client) {
        this.client = client;
    }

    public List<PedidoDto> listar() {
        return DownstreamErrors.call("ms-pedidos", () -> client.get().uri("/api/pedidos")
                .retrieve().body(new ParameterizedTypeReference<List<PedidoDto>>() { }));
    }

    public PedidoDto obtener(Long id) {
        return DownstreamErrors.call("ms-pedidos", () -> client.get().uri("/api/pedidos/{id}", id)
                .retrieve().body(PedidoDto.class));
    }

    public PedidoDto crear(NuevoPedidoDto pedido) {
        return DownstreamErrors.call("ms-pedidos", () -> client.post().uri("/api/pedidos")
                .contentType(MediaType.APPLICATION_JSON)
                .body(pedido)
                .retrieve().body(PedidoDto.class));
    }
}
