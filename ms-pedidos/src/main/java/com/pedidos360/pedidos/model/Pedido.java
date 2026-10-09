package com.pedidos360.pedidos.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "pedidos")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Claim "oid" del usuario que hizo el pedido. */
    @Column(name = "usuario_oid", nullable = false, length = 64)
    private String usuarioOid;

    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoPedido estado = EstadoPedido.PENDIENTE;

    @Column(name = "direccion_entrega", length = 250)
    private String direccionEntrega;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PedidoItem> items = new ArrayList<>();

    @Column(name = "creado_en", nullable = false, updatable = false)
    private Instant creadoEn;

    @Column(name = "actualizado_en")
    private Instant actualizadoEn;

    protected Pedido() {
    }

    public Pedido(String usuarioOid, Long clienteId, String direccionEntrega) {
        this.usuarioOid = usuarioOid;
        this.clienteId = clienteId;
        this.direccionEntrega = direccionEntrega;
    }

    public void agregarItem(PedidoItem item) {
        item.setPedido(this);
        items.add(item);
        total = total.add(item.getSubtotal());
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
    public Long getClienteId() { return clienteId; }
    public EstadoPedido getEstado() { return estado; }
    public void setEstado(EstadoPedido estado) { this.estado = estado; }
    public String getDireccionEntrega() { return direccionEntrega; }
    public BigDecimal getTotal() { return total; }
    public List<PedidoItem> getItems() { return items; }
    public Instant getCreadoEn() { return creadoEn; }
    public Instant getActualizadoEn() { return actualizadoEn; }
}
