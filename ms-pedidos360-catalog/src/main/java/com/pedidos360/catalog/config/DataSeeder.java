package com.pedidos360.catalog.config;

import com.pedidos360.catalog.model.Category;
import com.pedidos360.catalog.model.Product;
import com.pedidos360.catalog.repository.CategoryRepository;
import com.pedidos360.catalog.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public void run(String... args) {
        if (productRepository.count() > 0) {
            return;
        }

        Category cafe = saveCategory("Café");
        Category lacteos = saveCategory("Lácteos");

        Product cafeMolido = Product.builder()
                .name("Café molido 500g")
                .description("Café de grano tostado molido, marca la Finca")
                .price(new BigDecimal("4990"))
                .stock(25)
                .category(cafe)
                .build();

        Product leche = Product.builder()
                .name("Leche entera 1L")
                .description("Leche entera pasteurizada, caja 1 litro")
                .price(new BigDecimal("1190"))
                .stock(40)
                .category(lacteos)
                .build();

        productRepository.saveAll(List.of(cafeMolido, leche));
    }

    private Category saveCategory(String name) {
        return categoryRepository.findByName(name)
                .orElseGet(() -> categoryRepository.save(
                        Category.builder().name(name).build()));
    }
}