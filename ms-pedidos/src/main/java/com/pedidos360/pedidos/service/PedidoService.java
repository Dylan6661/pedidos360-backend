package com.pedidos360.pedidos.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pedidos360.common.error.ApiException;
import com.pedidos360.pedidos.dto.ItemRequest;
import com.pedidos360.pedidos.dto.PedidoRequest;
import com.pedidos360.pedidos.dto.PedidoResponse;
import com.pedidos360.pedidos.model.EstadoPedido;
import com.pedidos360.pedidos.model.Pedido;
import com.pedidos360.pedidos.model.PedidoItem;
import com.pedidos360.pedidos.repository.PedidoRepository;

@Service
@Transactional(readOnly = true)
public class PedidoService {

    private final PedidoRepository repository;

    public PedidoService(PedidoRepository repository) {
        this.repository = repository;
    }

    /** Un Admin ve todos los pedidos; un usuario normal solo los suyos. */
    public List<PedidoResponse> listar(String oid, boolean admin) {
        List<Pedido> pedidos = admin
                ? repository.findAllByOrderByCreadoEnDesc()
                : repository.findByUsuarioOidOrderByCreadoEnDesc(oid);
        return pedidos.stream().map(PedidoResponse::from).toList();
    }

    public PedidoResponse obtener(Long id, String oid, boolean admin) {
        return PedidoResponse.from(buscarAutorizado(id, oid, admin));
    }

    @Transactional
    public PedidoResponse crear(String oid, PedidoRequest req) {
        Pedido pedido = new Pedido(oid, req.clienteId(), req.direccionEntrega());
        for (ItemRequest i : req.items()) {
            pedido.agregarItem(new PedidoItem(i.productoId(), i.nombreProducto(), i.cantidad(), i.precioUnitario()));
        }
        return PedidoResponse.from(repository.save(pedido));
    }

    @Transactional
    public PedidoResponse cambiarEstado(Long id, EstadoPedido estado) {
        Pedido pedido = buscar(id);
        if (pedido.getEstado() == EstadoPedido.CANCELADO || pedido.getEstado() == EstadoPedido.ENTREGADO) {
            throw ApiException.conflict("El pedido ya está " + pedido.getEstado() + " y no puede cambiar de estado");
        }
        pedido.setEstado(estado);
        return PedidoResponse.from(pedido);
    }

    /** El dueño puede cancelar su pedido mientras esté PENDIENTE. */
    @Transactional
    public PedidoResponse cancelar(Long id, String oid, boolean admin) {
        Pedido pedido = buscarAutorizado(id, oid, admin);
        if (pedido.getEstado() != EstadoPedido.PENDIENTE) {
            throw ApiException.conflict("Solo se pueden cancelar pedidos en estado PENDIENTE");
        }
        pedido.setEstado(EstadoPedido.CANCELADO);
        return PedidoResponse.from(pedido);
    }

    @Transactional
    public void eliminar(Long id) {
        repository.delete(buscar(id));
    }

    private Pedido buscarAutorizado(Long id, String oid, boolean admin) {
        Pedido pedido = buscar(id);
        if (!admin && !pedido.getUsuarioOid().equals(oid)) {
            throw ApiException.forbidden("No puede acceder a pedidos de otro usuario");
        }
        return pedido;
    }

    private Pedido buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Pedido " + id + " no existe"));
    }
}
