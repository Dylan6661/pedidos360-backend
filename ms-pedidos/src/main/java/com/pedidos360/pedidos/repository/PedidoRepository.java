package com.pedidos360.pedidos.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.pedidos360.pedidos.model.Pedido;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    @EntityGraph(attributePaths = "items")
    List<Pedido> findByUsuarioOidOrderByCreadoEnDesc(String usuarioOid);

    @EntityGraph(attributePaths = "items")
    List<Pedido> findAllByOrderByCreadoEnDesc();
}
