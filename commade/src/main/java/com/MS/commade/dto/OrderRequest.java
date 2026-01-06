package com.MS.commade.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {

    private Long userId;

    @NotEmpty(message = "Order must contain at least one item")
    private List<OrderLineItemsDto> orderLineItemsDtoList;
}

