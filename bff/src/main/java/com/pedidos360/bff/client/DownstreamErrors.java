package com.pedidos360.bff.client;

import java.util.function.Supplier;

import org.springframework.http.HttpStatus;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pedidos360.common.error.ApiException;

/** Traduce los errores HTTP de los microservicios a ApiException con el mismo código. */
final class DownstreamErrors {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private DownstreamErrors() {
    }

    static <T> T call(String servicio, Supplier<T> call) {
        try {
            return call.get();
        } catch (RestClientResponseException ex) {
            HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
            throw new ApiException(status != null ? status : HttpStatus.BAD_GATEWAY,
                    servicio + ": " + extraerMensaje(ex));
        } catch (ResourceAccessException ex) {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, servicio + " no está disponible");
        }
    }

    private static String extraerMensaje(RestClientResponseException ex) {
        try {
            JsonNode node = MAPPER.readTree(ex.getResponseBodyAsString());
            if (node != null && node.hasNonNull("message")) {
                return node.get("message").asText();
            }
        } catch (Exception ignored) {
            // cuerpo no JSON
        }
        return ex.getStatusText();
    }
}
