package com.pedidos360.clientes.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.pedidos360.clientes.dto.ClienteRequest;
import com.pedidos360.clientes.dto.ClienteResponse;
import com.pedidos360.clientes.dto.PerfilRequest;
import com.pedidos360.clientes.model.Cliente;
import com.pedidos360.clientes.repository.ClienteRepository;
import com.pedidos360.common.error.ApiException;

@Service
@Transactional(readOnly = true)
public class ClienteService {

    private final ClienteRepository repository;

    public ClienteService(ClienteRepository repository) {
        this.repository = repository;
    }

    public ClienteResponse miPerfil(String oid) {
        return repository.findByUsuarioOid(oid)
                .map(ClienteResponse::from)
                .orElseThrow(() -> ApiException.notFound("El usuario aún no completa su perfil de cliente"));
    }

    /** Crea o actualiza el perfil de cliente asociado al usuario autenticado. */
    @Transactional
    public ClienteResponse guardarMiPerfil(String oid, String emailToken, PerfilRequest req) {
        String email = StringUtils.hasText(req.email()) ? req.email() : emailToken;
        if (!StringUtils.hasText(email)) {
            throw ApiException.badRequest("Debe indicar un email");
        }
        Cliente cliente = repository.findByUsuarioOid(oid)
                .orElseGet(() -> new Cliente(oid, req.nombre(), email, req.telefono(), req.direccion()));
        cliente.setNombre(req.nombre());
        cliente.setEmail(email);
        cliente.setTelefono(req.telefono());
        cliente.setDireccion(req.direccion());
        return ClienteResponse.from(repository.save(cliente));
    }

    public List<ClienteResponse> listar() {
        return repository.findAllByOrderByNombreAsc().stream().map(ClienteResponse::from).toList();
    }

    public ClienteResponse obtener(Long id) {
        return ClienteResponse.from(buscar(id));
    }

    @Transactional
    public ClienteResponse crear(ClienteRequest req) {
        if (repository.existsByEmailIgnoreCase(req.email())) {
            throw ApiException.conflict("Ya existe un cliente con el email " + req.email());
        }
        Cliente cliente = new Cliente(null, req.nombre(), req.email(), req.telefono(), req.direccion());
        return ClienteResponse.from(repository.save(cliente));
    }

    @Transactional
    public ClienteResponse actualizar(Long id, ClienteRequest req) {
        Cliente cliente = buscar(id);
        cliente.setNombre(req.nombre());
        cliente.setEmail(req.email());
        cliente.setTelefono(req.telefono());
        cliente.setDireccion(req.direccion());
        return ClienteResponse.from(cliente);
    }

    @Transactional
    public void eliminar(Long id) {
        repository.delete(buscar(id));
    }

    private Cliente buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Cliente " + id + " no existe"));
    }
}
