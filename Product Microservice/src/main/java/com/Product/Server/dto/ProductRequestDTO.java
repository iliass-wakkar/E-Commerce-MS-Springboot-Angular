package com.Product.Server.dto;

import lombok.Data;

@Data
public class ProductRequestDTO {
    private String name;
    private String description;
    private double price;
    private int stockQuantity;
    private String imageUrl;
    private String manufacturer;
    private Long categoryId;
}
