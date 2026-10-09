package com.pedidos360.pedidos.dto;

import com.pedidos360.pedidos.model.EstadoPedido;

import jakarta.validation.constraints.NotNull;

public record EstadoRequest(@NotNull EstadoPedido estado) {
}
