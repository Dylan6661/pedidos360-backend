package com.pedidos360.bff.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.pedidos360.bff.client.ClientesClient;
import com.pedidos360.bff.client.PedidosClient;
import com.pedidos360.bff.client.ProductosClient;
import com.pedidos360.bff.dto.ClienteDto;
import com.pedidos360.bff.dto.CrearPedidoRequest;
import com.pedidos360.bff.dto.NuevoPedidoDto;
import com.pedidos360.bff.dto.PedidoDetalleResponse;
import com.pedidos360.bff.dto.PedidoDto;
import com.pedidos360.bff.dto.ProductoDto;
import com.pedidos360.bff.dto.ResumenResponse;
import com.pedidos360.common.error.ApiException;
import com.pedidos360.common.security.CurrentUser;

/** Orquesta los microservicios y entrega al frontend respuestas listas para mostrar. */
@Service
public class BffService {

    private final ProductosClient productos;
    private final ClientesClient clientes;
    private final PedidosClient pedidos;

    public BffService(ProductosClient productos, ClientesClient clientes, PedidosClient pedidos) {
        this.productos = productos;
        this.clientes = clientes;
        this.pedidos = pedidos;
    }

    public ResumenResponse resumen(Jwt jwt) {
        ClienteDto cliente = clientes.miPerfil().orElse(null);
        List<PedidoDto> misPedidos = pedidos.listar();
        List<ProductoDto> catalogo = productos.listar();

        BigDecimal totalGastado = misPedidos.stream()
                .filter(p -> !"CANCELADO".equals(p.estado()))
                .map(PedidoDto::total)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long pendientes = misPedidos.stream().filter(p -> "PENDIENTE".equals(p.estado())).count();

        ResumenResponse.Usuario usuario = new ResumenResponse.Usuario(
                CurrentUser.oid(jwt), CurrentUser.name(jwt), CurrentUser.email(jwt),
                CurrentUser.roles(jwt), CurrentUser.scopes(jwt));

        return new ResumenResponse(usuario, cliente != null, cliente, misPedidos.size(), pendientes,
                totalGastado, catalogo.size(), misPedidos.stream().limit(5).toList());
    }

    /** Catálogo agrupado por categoría para la vista de compra. */
    public Map<String, List<ProductoDto>> catalogo() {
        return productos.listar().stream()
                .collect(Collectors.groupingBy(ProductoDto::categoria, TreeMap::new, Collectors.toList()));
    }

    /**
     * Crea un pedido: obtiene el perfil del cliente, toma precios y stock reales desde ms-productos
     * (el frontend nunca envía precios) y registra el pedido en ms-pedidos.
     */
    public PedidoDetalleResponse crearPedido(CrearPedidoRequest req) {
        ClienteDto cliente = clientes.miPerfil()
                .orElseThrow(() -> ApiException.badRequest("Complete su perfil de cliente antes de hacer un pedido"));

        // Agrupa líneas repetidas del mismo producto
        Map<Long, Integer> cantidades = new LinkedHashMap<>();
        for (CrearPedidoRequest.Linea linea : req.items()) {
            cantidades.merge(linea.productoId(), linea.cantidad(), Integer::sum);
        }

        List<NuevoPedidoDto.Item> items = new ArrayList<>();
        for (Map.Entry<Long, Integer> e : cantidades.entrySet()) {
            ProductoDto producto = productos.obtener(e.getKey());
            if (producto.stock() < e.getValue()) {
                throw ApiException.conflict("Stock insuficiente para " + producto.nombre()
                        + " (disponible: " + producto.stock() + ")");
            }
            items.add(new NuevoPedidoDto.Item(producto.id(), producto.nombre(), e.getValue(), producto.precio()));
        }

        String direccion = StringUtils.hasText(req.direccionEntrega()) ? req.direccionEntrega() : cliente.direccion();
        PedidoDto creado = pedidos.crear(new NuevoPedidoDto(cliente.id(), direccion, items));
        return new PedidoDetalleResponse(creado, cliente);
    }

    public PedidoDetalleResponse detalle(Long id, Jwt jwt) {
        PedidoDto pedido = pedidos.obtener(id); // ms-pedidos valida que sea del usuario o que sea Admin
        ClienteDto cliente = CurrentUser.isAdmin(jwt)
                ? clientes.obtener(pedido.clienteId())
                : clientes.miPerfil().orElse(null);
        return new PedidoDetalleResponse(pedido, cliente);
    }
}
