package com.MS.commade.controller;

import com.MS.commade.dto.CartResponse;
import com.MS.commade.services.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<CartResponse> getCart(
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(cartService.getCart(userId));
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponse> addToCart(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestParam Long productId,
            @RequestParam Integer quantity) {
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(cartService.addToCart(userId, productId, quantity));
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<CartResponse> removeFromCart(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @PathVariable Long productId) {
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(cartService.removeFromCart(userId, productId));
    }

    @PutMapping("/items")
    public ResponseEntity<CartResponse> updateCartItemQuantity(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestParam Long productId,
            @RequestParam Integer quantity) {
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(cartService.updateCartItemQuantity(userId, productId, quantity));
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart(
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        cartService.clearCart(userId);
        return ResponseEntity.ok().build();
    }
}
