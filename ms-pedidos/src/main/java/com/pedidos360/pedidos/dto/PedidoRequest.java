package com.pedidos360.pedidos.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Pedido ya validado por el BFF (precios y stock tomados desde ms-productos).
 * El usuario dueño del pedido se toma del token, nunca del cuerpo.
 */
public record PedidoRequest(
        @NotNull Long clienteId,
        @Size(max = 250) String direccionEntrega,
        @NotEmpty @Valid List<ItemRequest> items) {
}
