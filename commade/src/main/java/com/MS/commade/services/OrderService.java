package com.MS.commade.services;

import com.MS.commade.clients.ProductRestClient;
import com.MS.commade.clients.ClientRestClient;
import com.MS.commade.dto.ProductDto;
import com.MS.commade.dto.UserDto;
import com.MS.commade.dto.OrderRequest;
import com.MS.commade.dto.OrderResponse;
import com.MS.commade.entities.Order;
import com.MS.commade.entities.OrderItem;
import com.MS.commade.mapper.OrderMapper;
import com.MS.commade.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional // Ensures if one item fails, the whole order is canceled
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRestClient productRestClient;
    private final ClientRestClient clientRestClient;
    private final OrderMapper orderMapper;

    /**
     * Creates an order from a cart (list of items).
     *
     * @param orderRequest the order request containing a list of items
     * @return the saved Order object with all items
     * @throws IllegalArgumentException if any product is unavailable or out of stock
     */
    public Order placeOrder(OrderRequest orderRequest) {
        // Validate that the cart is not empty
        if (orderRequest.getOrderLineItemsDtoList() == null || orderRequest.getOrderLineItemsDtoList().isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }

        // 0. Verify User exists
        UserDto user = clientRestClient.getUserById(orderRequest.getUserId());
        if (user == null) {
            throw new IllegalArgumentException("User with ID " + orderRequest.getUserId() + " not found");
        }

        // Create the Order header
        Order order = Order.builder()
            .orderNumber(UUID.randomUUID().toString())
            .userId(orderRequest.getUserId())
            .status("CREATED")
            .build();

        // 1. Convert DTOs to Entities & Calculate Total
        List<OrderItem> orderItems = orderRequest.getOrderLineItemsDtoList()
            .stream()
            .map(itemDto -> {
                // CALL PRODUCT MS (Synchronous via Feign)
                ProductDto product = productRestClient.getProductById(itemDto.getProductId());

                // Check if product is available (fallback returns null ID)
                if (product == null || product.getId() == null) {
                    throw new IllegalArgumentException("Product with ID " + itemDto.getProductId() + " is unavailable");
                }

                // Verify Stock
                if (product.getStockQuantity() < itemDto.getQuantity()) {
                    throw new IllegalArgumentException("Product '" + product.getName() + "' is out of stock. " +
                        "Available: " + product.getStockQuantity() + ", Requested: " + itemDto.getQuantity());
                }

                // Create OrderItem with the real price from Product MS
                return OrderItem.builder()
                    .productId(itemDto.getProductId())
                    .quantity(itemDto.getQuantity())
                    .price(product.getPrice()) // Use real price from Product DB
                    .order(order)
                    .build();
            })
            .collect(Collectors.toList());

        // Set items to order
        order.setOrderLineItems(orderItems);

        // 2. Calculate total price
        double total = orderItems.stream()
            .mapToDouble(item -> item.getPrice() * item.getQuantity())
            .sum();
        order.setTotalPrice(total);

        // 3. Save Order (Cascading saves Items too)
        return orderRepository.save(order);
    }

    /**
     * Retrieves an order by its ID.
     *
     * @param id the order ID
     * @return the OrderResponse DTO with all items
     * @throws IllegalArgumentException if order is not found
     */
    public OrderResponse getOrderById(Long id) {
        return orderRepository.findById(id)
                .map(orderMapper::fromEntity)
                .orElseThrow(() -> new IllegalArgumentException("Order with ID " + id + " not found"));
    }

    /**
     * Retrieves all orders.
     *
     * @return list of OrderResponse DTOs
     */
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(orderMapper::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves orders by user ID.
     *
     * @param userId the user ID
     * @return list of OrderResponse DTOs
     */
    public List<OrderResponse> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserId(userId).stream()
                .map(orderMapper::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Updates the status of an order.
     *
     * @param id the order ID
     * @param status the new status
     * @return the updated OrderResponse
     */
    public OrderResponse updateOrderStatus(Long id, String status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order with ID " + id + " not found"));
        order.setStatus(status);
        Order savedOrder = orderRepository.save(order);
        return orderMapper.fromEntity(savedOrder);
    }
}
