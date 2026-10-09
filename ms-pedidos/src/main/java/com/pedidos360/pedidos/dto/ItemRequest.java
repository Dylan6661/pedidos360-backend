package com.pedidos360.pedidos.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ItemRequest(
        @NotNull Long productoId,
        @NotBlank String nombreProducto,
        @NotNull @Min(1) Integer cantidad,
        @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal precioUnitario) {
}
