package com.pedidos360.pedidos.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import com.pedidos360.pedidos.model.EstadoPedido;
import com.pedidos360.pedidos.model.Pedido;
import com.pedidos360.pedidos.model.PedidoItem;

public record PedidoResponse(Long id, Long clienteId, String usuarioOid, EstadoPedido estado,
                             String direccionEntrega, BigDecimal total, Instant creadoEn,
                             List<Item> items) {

    public record Item(Long productoId, String nombreProducto, Integer cantidad,
                       BigDecimal precioUnitario, BigDecimal subtotal) {

        static Item from(PedidoItem i) {
            return new Item(i.getProductoId(), i.getNombreProducto(), i.getCantidad(),
                    i.getPrecioUnitario(), i.getSubtotal());
        }
    }

    public static PedidoResponse from(Pedido p) {
        return new PedidoResponse(p.getId(), p.getClienteId(), p.getUsuarioOid(), p.getEstado(),
                p.getDireccionEntrega(), p.getTotal(), p.getCreadoEn(),
                p.getItems().stream().map(Item::from).toList());
    }
}
