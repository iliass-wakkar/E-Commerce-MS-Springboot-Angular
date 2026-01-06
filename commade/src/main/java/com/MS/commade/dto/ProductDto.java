package com.MS.commade.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {

    private Long id;
    private String name;
    private Double price;
    private String imageUrl; // URL of the product image used in CartService
    private Integer stockQuantity; // Stock quantity from Product MS

    // Explicit getters for environments without Lombok processing
    public Long getId() { return id; }
    public String getName() { return name; }
    public Double getPrice() { return price; }
    public String getImageUrl() { return imageUrl; }
    public Integer getStockQuantity() { return stockQuantity; }

    // Manual builder to work without Lombok if needed
    public static ProductDtoBuilder builder() { return new ProductDtoBuilder(); }

    public static class ProductDtoBuilder {
        private Long id;
        private String name;
        private Double price;
        private String imageUrl;
        private Integer stockQuantity;

        public ProductDtoBuilder id(Long id) { this.id = id; return this; }
        public ProductDtoBuilder name(String name) { this.name = name; return this; }
        public ProductDtoBuilder price(Double price) { this.price = price; return this; }
        public ProductDtoBuilder imageUrl(String imageUrl) { this.imageUrl = imageUrl; return this; }
        public ProductDtoBuilder stockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; return this; }

        public ProductDto build() {
            ProductDto dto = new ProductDto();
            dto.id = this.id;
            dto.name = this.name;
            dto.price = this.price;
            dto.imageUrl = this.imageUrl;
            dto.stockQuantity = this.stockQuantity;
            return dto;
        }
    }
}
