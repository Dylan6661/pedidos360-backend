package com.pedidos360.clientes.model;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "clientes")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Claim "oid" del usuario en Entra External ID (vincula la cuenta con su perfil). */
    @Column(name = "usuario_oid", unique = true, length = 64)
    private String usuarioOid;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(nullable = false, length = 160)
    private String email;

    @Column(length = 30)
    private String telefono;

    @Column(length = 250)
    private String direccion;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private Instant creadoEn;

    @Column(name = "actualizado_en")
    private Instant actualizadoEn;

    protected Cliente() {
    }

    public Cliente(String usuarioOid, String nombre, String email, String telefono, String direccion) {
        this.usuarioOid = usuarioOid;
        this.nombre = nombre;
        this.email = email;
        this.telefono = telefono;
        this.direccion = direccion;
    }

    @PrePersist
    void onCreate() {
        creadoEn = Instant.now();
    }

    @PreUpdate
    void onUpdate() {
        actualizadoEn = Instant.now();
    }

    public Long getId() { return id; }
    public String getUsuarioOid() { return usuarioOid; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public Instant getCreadoEn() { return creadoEn; }
}
