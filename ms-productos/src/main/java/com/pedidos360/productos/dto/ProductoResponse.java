package com.pedidos360.productos.dto;

import java.math.BigDecimal;

import com.pedidos360.productos.model.Producto;

public record ProductoResponse(Long id, String nombre, String descripcion, String categoria,
                               BigDecimal precio, Integer stock) {

    public static ProductoResponse from(Producto p) {
        return new ProductoResponse(p.getId(), p.getNombre(), p.getDescripcion(), p.getCategoria(),
                p.getPrecio(), p.getStock());
    }
}
