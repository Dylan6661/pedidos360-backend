package com.pedidos360.bff.controller;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pedidos360.bff.dto.CrearPedidoRequest;
import com.pedidos360.bff.dto.PedidoDetalleResponse;
import com.pedidos360.bff.dto.ProductoDto;
import com.pedidos360.bff.dto.ResumenResponse;
import com.pedidos360.bff.service.BffService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/bff")
public class BffController {

    private final BffService service;

    public BffController(BffService service) {
        this.service = service;
    }

    @GetMapping("/resumen")
    public ResumenResponse resumen(@AuthenticationPrincipal Jwt jwt) {
        return service.resumen(jwt);
    }

    @GetMapping("/catalogo")
    public Map<String, List<ProductoDto>> catalogo() {
        return service.catalogo();
    }

    @PostMapping("/pedidos")
    public ResponseEntity<PedidoDetalleResponse> crearPedido(@Valid @RequestBody CrearPedidoRequest request) {
        PedidoDetalleResponse creado = service.crearPedido(request);
        return ResponseEntity.created(URI.create("/api/bff/pedidos/" + creado.pedido().id() + "/detalle"))
                .body(creado);
    }

    @GetMapping("/pedidos/{id}/detalle")
    public PedidoDetalleResponse detalle(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return service.detalle(id, jwt);
    }
}
