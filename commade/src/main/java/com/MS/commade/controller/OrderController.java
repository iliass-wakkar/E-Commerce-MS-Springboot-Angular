package com.MS.commade.controller;

import com.MS.commade.dto.OrderResponse;
import com.MS.commade.dto.OrderRequest;
import com.MS.commade.entities.Order;
import com.MS.commade.services.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * Creates a new order from a cart (list of items).
     *
     * @param orderRequest the order request containing a list of items (cart)
     */
    @PostMapping
    public ResponseEntity<OrderResponse> placeOrder(
            @RequestHeader(value = "X-User-Id", required = true) Long userId,
            @RequestBody @Valid OrderRequest orderRequest) {
        try {
            System.out.println("Received order request for user: " + userId);
            // Set the userId from the trusted header
            orderRequest.setUserId(userId);

            Order createdOrder = orderService.placeOrder(orderRequest);
            // Convert Entity to DTO for response
            OrderResponse response = orderService.getOrderById(createdOrder.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            System.err.println("Order validation failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            System.err.println("Order creation failed: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Retrieves an order by its ID.
     *
     * @param id the order ID
     * @return the OrderResponse with all items and 200 OK status
     */
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long id) {
        try {
            OrderResponse order = orderService.getOrderById(id);
            return ResponseEntity.ok(order);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Retrieves all orders.
     *
     * @return list of all orders with 200 OK status
     */
    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAllOrders(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        try {
            List<OrderResponse> orders;
            if ("ADMIN".equals(userRole)) {
                orders = orderService.getAllOrders();
            } else if (userId != null) {
                orders = orderService.getOrdersByUserId(userId);
            } else {
                // Fallback if headers are missing (e.g. direct call without gateway)
                // For security, maybe return empty or 403, but for now let's return empty
                orders = List.of();
            }
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Updates the status of an order.
     *
     * @param id the order ID
     * @param status the new status
     * @return the updated OrderResponse
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(@PathVariable Long id, @RequestBody String status) {
        try {
            // Remove quotes if present (simple sanitization)
            String cleanStatus = status.replace("\"", "");
            OrderResponse updatedOrder = orderService.updateOrderStatus(id, cleanStatus);
            return ResponseEntity.ok(updatedOrder);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

