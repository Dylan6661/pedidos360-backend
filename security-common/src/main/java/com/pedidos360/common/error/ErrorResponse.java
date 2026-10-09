package com.pedidos360.common.error;

import java.time.Instant;

import org.springframework.http.HttpStatus;

/** Formato único de error para todas las APIs de Pedidos360. */
public record ErrorResponse(Instant timestamp, int status, String error, String message, String path) {

    public static ErrorResponse of(HttpStatus status, String message, String path) {
        return new ErrorResponse(Instant.now(), status.value(), status.getReasonPhrase(), message, path);
    }
}
