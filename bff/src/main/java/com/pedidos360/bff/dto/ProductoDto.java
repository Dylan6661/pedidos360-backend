package com.pedidos360.bff.dto;

import java.math.BigDecimal;

public record ProductoDto(Long id, String nombre, String descripcion, String categoria,
                          BigDecimal precio, Integer stock) {
}
