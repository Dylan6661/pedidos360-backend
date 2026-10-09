package com.pedidos360.productos.config;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.pedidos360.productos.model.Producto;
import com.pedidos360.productos.repository.ProductoRepository;

/** Carga un catálogo inicial si la tabla está vacía (útil para la demo). */
@Component
@ConditionalOnProperty(name = "pedidos360.seed-data", havingValue = "true", matchIfMissing = true)
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final ProductoRepository repository;

    public DataInitializer(ProductoRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        if (repository.count() > 0) {
            return;
        }
        repository.save(new Producto("Hamburguesa clásica", "Carne 150 g, queso cheddar y tomate", "Comida", new BigDecimal("6990"), 50));
        repository.save(new Producto("Pizza margarita", "Pizza mediana de tomate, mozzarella y albahaca", "Comida", new BigDecimal("9990"), 30));
        repository.save(new Producto("Ensalada César", "Lechuga, pollo grillado, crutones y parmesano", "Comida", new BigDecimal("5490"), 25));
        repository.save(new Producto("Bebida 500 ml", "Bebida gaseosa en lata o botella", "Bebestible", new BigDecimal("1490"), 120));
        repository.save(new Producto("Jugo natural", "Jugo de fruta de la estación 400 ml", "Bebestible", new BigDecimal("2490"), 40));
        repository.save(new Producto("Brownie", "Brownie de chocolate con nueces", "Postre", new BigDecimal("2990"), 35));
        log.info("Catálogo inicial cargado: {} productos", repository.count());
    }
}
