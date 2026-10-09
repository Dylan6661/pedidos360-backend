package com.pedidos360.clientes.dto;

import java.time.Instant;

import com.pedidos360.clientes.model.Cliente;

public record ClienteResponse(Long id, String usuarioOid, String nombre, String email,
                              String telefono, String direccion, Instant creadoEn) {

    public static ClienteResponse from(Cliente c) {
        return new ClienteResponse(c.getId(), c.getUsuarioOid(), c.getNombre(), c.getEmail(),
                c.getTelefono(), c.getDireccion(), c.getCreadoEn());
    }
}
