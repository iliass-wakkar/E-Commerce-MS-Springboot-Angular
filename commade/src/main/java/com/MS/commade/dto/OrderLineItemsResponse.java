package com.MS.commade.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderLineItemsResponse {
    private Long id;
    private Long productId;
    private Integer quantity;
    private Double price;
}

