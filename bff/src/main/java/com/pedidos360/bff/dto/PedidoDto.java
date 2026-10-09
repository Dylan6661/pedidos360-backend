package com.pedidos360.bff.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record PedidoDto(Long id, Long clienteId, String usuarioOid, String estado,
                        String direccionEntrega, BigDecimal total, Instant creadoEn,
                        List<Item> items) {

    public record Item(Long productoId, String nombreProducto, Integer cantidad,
                       BigDecimal precioUnitario, BigDecimal subtotal) {
    }
}
