package com.pedidos360.bff.dto;

import java.math.BigDecimal;
import java.util.List;

public record ResumenResponse(Usuario usuario, boolean perfilCompleto, ClienteDto cliente,
                              int totalPedidos, long pedidosPendientes, BigDecimal totalGastado,
                              int productosDisponibles, List<PedidoDto> ultimosPedidos) {

    public record Usuario(String oid, String nombre, String email, List<String> roles, List<String> scopes) {
    }
}
