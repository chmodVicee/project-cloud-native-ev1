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

        Category bebidas = saveCategory("Bebidas");
        Category snacks = saveCategory("Snacks");
        Category cafe = saveCategory("Café");
        Category lacteos = saveCategory("Lácteos");
        Category despensa = saveCategory("Despensa");
        Category limpieza = saveCategory("Limpieza");
        Category te = saveCategory("Té");

        List<Product> products = List.of(
                product("Coca-Cola original 1.5L", "Bebida gaseosa cola, botella 1.5 litros", "2490", 30, bebidas),
                product("Sprite 2L", "Bebida gaseosa lima-limón, botella 2 litros", "2490", 30, bebidas),
                product("Fanta naranja 2L", "Bebida gaseosa naranja, botella 2 litros", "2490", 25, bebidas),
                product("Agua mineral sin gas 1.5L", "Agua mineralizada, botella 1.5 litros", "990", 50, bebidas),
                product("Jugo natural de piña 1L", "Jugo de piña sin conservantes, envase 1 litro", "1990", 20, bebidas),
                product("Bebida energética 473ml", "Bebida energética sabor clásico, lata 473 ml", "1790", 20, bebidas),

                product("Papas fritas clásicas 120g", "Papas fritas sabor original, bolsa 120 gramos", "1290", 35, snacks),
                product("Galletas de chocolate 200g", "Galletas con trozos de chocolate, paquete 200 gramos", "1490", 40, snacks),
                product("Maní salado 200g", "Maní tostado con sal, bolsa 200 gramos", "1590", 25, snacks),
                product("Barra de cereal frutas 30g", "Barra de cereal con frutas, 30 gramos", "990", 30, snacks),

                product("Café molido tostado 500g", "Café de grano tostado molido, marca la Finca", "4990", 25, cafe),
                product("Café soluble 200g", "Café soluble instantáneo, frasco 200 gramos", "2990", 20, cafe),
                product("Té verde 25 bolsitas", "Té verde en caja de 25 bolsitas filtrantes", "1590", 30, te),

                product("Leche entera 1L", "Leche entera pasteurizada, caja 1 litro", "1190", 40, lacteos),
                product("Leche descremada 1L", "Leche descremada pasteurizada, caja 1 litro", "1190", 30, lacteos),
                product("Yogurt natural 1kg", "Yogurt natural sin sabor, tarro 1 kilo", "2490", 25, lacteos),
                product("Queso mantecoso 200g", "Queso mantecoso chileno, bloque 200 gramos", "2790", 20, lacteos),
                product("Mantequilla 250g", "Mantequilla con sal, barra 250 gramos", "1990", 25, lacteos),

                product("Arroz grano largo 1kg", "Arroz de grano largo, paquete 1 kilo", "1390", 45, despensa),
                product("Aceite vegetal 1L", "Aceite vegetal mezcla, botella 1 litro", "2590", 25, despensa),
                product("Tallarines 400g", "Fideos tallarines de sémola, paquete 400 gramos", "890", 50, despensa),
                product("Azúcar refinada 1kg", "Azúcar blanca refinada, paquete 1 kilo", "1090", 35, despensa),
                product("Sal fina 500g", "Sal de mesa fina, envase 500 gramos", "590", 40, despensa),

                product("Detergente ropa 3kg", "Detergente en polvo para lavadora, 3 kilos", "3990", 15, limpieza),
                product("Jabón líquido loza 750ml", "Jabón líquido para lavar loza, botella 750 ml", "2190", 20, limpieza));

        productRepository.saveAll(products);
    }

    private Category saveCategory(String name) {
        return categoryRepository.findByName(name)
                .orElseGet(() -> categoryRepository.save(
                        Category.builder().name(name).build()));
    }

    private Product product(String name, String description, String price, int stock, Category category) {
        return Product.builder()
                .name(name)
                .description(description)
                .price(new BigDecimal(price))
                .stock(stock)
                .category(category)
                .build();
    }
}