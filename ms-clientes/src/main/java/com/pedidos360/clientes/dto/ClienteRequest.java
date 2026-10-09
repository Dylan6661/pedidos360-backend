package com.pedidos360.clientes.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Alta o edición de un cliente por parte de un administrador. */
public record ClienteRequest(
        @NotBlank @Size(max = 120) String nombre,
        @NotBlank @Email @Size(max = 160) String email,
        @Size(max = 30) String telefono,
        @Size(max = 250) String direccion) {
}
