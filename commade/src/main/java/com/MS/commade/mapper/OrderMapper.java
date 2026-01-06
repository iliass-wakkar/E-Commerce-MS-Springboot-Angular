package com.MS.commade.mapper;

import com.MS.commade.dto.OrderLineItemsResponse;
import com.MS.commade.dto.OrderResponse;
import com.MS.commade.entities.Order;
import com.MS.commade.entities.OrderItem;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class OrderMapper {

    public OrderResponse fromEntity(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .orderDate(order.getOrderDate())
                .status(order.getStatus())
                .totalPrice(order.getTotalPrice())
                .userId(order.getUserId())
                .orderLineItems(order.getOrderLineItems().stream()
                        .map(this::fromOrderItem)
                        .collect(Collectors.toList()))
                .build();
    }

    private OrderLineItemsResponse fromOrderItem(OrderItem item) {
        return OrderLineItemsResponse.builder()
                .id(item.getId())
                .productId(item.getProductId())
                .quantity(item.getQuantity())
                .price(item.getPrice())
                .build();
    }
}

