package com.pedidos360.productos.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.pedidos360.common.error.ApiException;
import com.pedidos360.productos.dto.ProductoRequest;
import com.pedidos360.productos.dto.ProductoResponse;
import com.pedidos360.productos.model.Producto;
import com.pedidos360.productos.repository.ProductoRepository;

@Service
@Transactional(readOnly = true)
public class ProductoService {

    private final ProductoRepository repository;

    public ProductoService(ProductoRepository repository) {
        this.repository = repository;
    }

    public List<ProductoResponse> listar(String categoria) {
        List<Producto> productos = StringUtils.hasText(categoria)
                ? repository.findByActivoTrueAndCategoriaIgnoreCaseOrderByNombreAsc(categoria)
                : repository.findByActivoTrueOrderByNombreAsc();
        return productos.stream().map(ProductoResponse::from).toList();
    }

    public ProductoResponse obtener(Long id) {
        return ProductoResponse.from(buscar(id));
    }

    @Transactional
    public ProductoResponse crear(ProductoRequest req) {
        Producto producto = new Producto(req.nombre(), req.descripcion(), req.categoria(), req.precio(), req.stock());
        return ProductoResponse.from(repository.save(producto));
    }

    @Transactional
    public ProductoResponse actualizar(Long id, ProductoRequest req) {
        Producto producto = buscar(id);
        producto.setNombre(req.nombre());
        producto.setDescripcion(req.descripcion());
        producto.setCategoria(req.categoria());
        producto.setPrecio(req.precio());
        producto.setStock(req.stock());
        return ProductoResponse.from(producto);
    }

    /** Eliminación lógica: el producto deja de aparecer en el catálogo pero se conserva el historial. */
    @Transactional
    public void eliminar(Long id) {
        buscar(id).setActivo(false);
    }

    private Producto buscar(Long id) {
        return repository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> ApiException.notFound("Producto " + id + " no existe"));
    }
}
