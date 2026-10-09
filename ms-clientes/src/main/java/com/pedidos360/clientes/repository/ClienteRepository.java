package com.pedidos360.clientes.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pedidos360.clientes.model.Cliente;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    Optional<Cliente> findByUsuarioOid(String usuarioOid);

    List<Cliente> findAllByOrderByNombreAsc();

    boolean existsByEmailIgnoreCase(String email);
}
