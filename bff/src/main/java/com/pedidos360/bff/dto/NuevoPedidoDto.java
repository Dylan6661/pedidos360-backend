package com.pedidos360.bff.dto;

import java.math.BigDecimal;
import java.util.List;

/** Cuerpo que el BFF envía a ms-pedidos, con precios tomados desde ms-productos. */
public record NuevoPedidoDto(Long clienteId, String direccionEntrega, List<Item> items) {

    public record Item(Long productoId, String nombreProducto, Integer cantidad, BigDecimal precioUnitario) {
    }
}
