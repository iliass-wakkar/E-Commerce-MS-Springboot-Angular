package com.Product.Server.dto;

import lombok.Data;

import java.time.Instant;

@Data
public class ProductResponseDTO {
    private Long id;
    private String name;
    private double price;
    private int stockQuantity;
    private String imageUrl;
    private String manufacturer;
    private Instant createdAt;
    private Instant updatedAt;
    private CategoryDTO productCategory;
}
