package com.Product.Server.config;

import com.Product.Server.model.Category;
import com.Product.Server.model.Product;
import com.Product.Server.repository.CategoryRepository;
import com.Product.Server.repository.ProductRepository;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ResourceLoader resourceLoader;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (productRepository.count() > 0) {
            // Data already exists, skip initialization
            return;
        }

        // Load the JSON file from the classpath
        Resource resource = resourceLoader.getResource("classpath:static/test-data.json");
        InputStream inputStream = resource.getInputStream();
        ObjectMapper mapper = new ObjectMapper();

        // Define a TypeReference to deserialize the list of products
        TypeReference<List<JsonProduct>> typeReference = new TypeReference<>() {};
        List<JsonProduct> jsonProducts = mapper.readValue(inputStream, typeReference);

        Map<String, Category> categoryMap = new HashMap<>();

        for (JsonProduct jsonProduct : jsonProducts) {
            // Get or create the category
            Category category = categoryMap.computeIfAbsent(jsonProduct.getProductCategory().getName(), name -> {
                Category newCat = new Category();
                newCat.setName(name);
                return categoryRepository.save(newCat);
            });

            // Create the product entity
            Product product = new Product();
            product.setName(jsonProduct.getName());
            product.setPrice(jsonProduct.getPrice());
            product.setImageUrl(jsonProduct.getImage());
            product.setDescription(jsonProduct.getDescription());
            product.setManufacturer(jsonProduct.getManufacturer());
            product.setCategory(category);
            // Generate a random stock quantity as it's not in the JSON
            product.setStockQuantity(ThreadLocalRandom.current().nextInt(10, 251));

            productRepository.save(product);
        }
    }

    // --- DTOs for JSON Deserialization ---

    @Data
    private static class JsonProduct {
        private String name;
        private double price;
        private String image;
        private String description;
        private String manufacturer;
        @JsonProperty("product_category")
        private JsonCategory productCategory;
    }

    @Data
    private static class JsonCategory {
        private String name;
    }
}
