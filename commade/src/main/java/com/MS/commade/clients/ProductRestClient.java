package com.MS.commade.clients;

import com.MS.commade.dto.ProductDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "product-service")
public interface ProductRestClient {

    @GetMapping("/products/{id}")
    @CircuitBreaker(name = "productService", fallbackMethod = "fallbackGetProductById")
    ProductDto getProductById(@PathVariable("id") Long id);

    // Fallback method: Throw exception instead of returning free product
    default ProductDto fallbackGetProductById(@PathVariable("id") Long id, Exception e) {
        throw new RuntimeException("Product Service is unavailable. Cannot verify product " + id);
    }
}

