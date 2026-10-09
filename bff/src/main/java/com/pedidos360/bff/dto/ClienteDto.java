package com.pedidos360.bff.dto;

public record ClienteDto(Long id, String usuarioOid, String nombre, String email,
                         String telefono, String direccion) {
}
