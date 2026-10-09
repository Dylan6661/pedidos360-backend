package com.pedidos360.clientes.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Datos que el propio usuario completa en su perfil. Si no envía email se usa el del token. */
public record PerfilRequest(
        @NotBlank @Size(max = 120) String nombre,
        @Email @Size(max = 160) String email,
        @Size(max = 30) String telefono,
        @NotBlank @Size(max = 250) String direccion) {
}
