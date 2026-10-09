package com.pedidos360.bff.client;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.pedidos360.bff.dto.ClienteDto;
import com.pedidos360.common.error.ApiException;

@Component
public class ClientesClient {

    private final RestClient client;

    public ClientesClient(@Qualifier("clientesRestClient") RestClient client) {
        this.client = client;
    }

    /** Perfil del usuario autenticado; vacío si aún no lo completa. */
    public Optional<ClienteDto> miPerfil() {
        try {
            return Optional.ofNullable(DownstreamErrors.call("ms-clientes", () -> client.get().uri("/api/clientes/me")
                    .retrieve().body(ClienteDto.class)));
        } catch (ApiException ex) {
            if (ex.getStatus() == HttpStatus.NOT_FOUND) {
                return Optional.empty();
            }
            throw ex;
        }
    }

    public ClienteDto obtener(Long id) {
        return DownstreamErrors.call("ms-clientes", () -> client.get().uri("/api/clientes/{id}", id)
                .retrieve().body(ClienteDto.class));
    }
}
