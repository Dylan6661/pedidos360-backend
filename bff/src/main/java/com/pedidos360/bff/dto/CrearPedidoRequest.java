package com.pedidos360.bff.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Lo que envía el frontend: solo productos y cantidades. Los precios los resuelve el BFF. */
public record CrearPedidoRequest(
        @Size(max = 250) String direccionEntrega,
        @NotEmpty @Valid List<Linea> items) {

    public record Linea(@NotNull Long productoId, @NotNull @Min(1) @Max(99) Integer cantidad) {
    }
}
