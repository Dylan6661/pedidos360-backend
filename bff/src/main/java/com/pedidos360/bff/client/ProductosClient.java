package com.pedidos360.bff.client;

import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.pedidos360.bff.dto.ProductoDto;

@Component
public class ProductosClient {

    private final RestClient client;

    public ProductosClient(@Qualifier("productosRestClient") RestClient client) {
        this.client = client;
    }

    public List<ProductoDto> listar() {
        return DownstreamErrors.call("ms-productos", () -> client.get().uri("/api/productos")
                .retrieve().body(new ParameterizedTypeReference<List<ProductoDto>>() { }));
    }

    public ProductoDto obtener(Long id) {
        return DownstreamErrors.call("ms-productos", () -> client.get().uri("/api/productos/{id}", id)
                .retrieve().body(ProductoDto.class));
    }
}
