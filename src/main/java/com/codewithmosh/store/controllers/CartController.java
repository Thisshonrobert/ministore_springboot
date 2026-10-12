//member 4
package com.codewithmosh.store.controllers;

import com.codewithmosh.store.dtos.*;
import com.codewithmosh.store.services.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.net.URI;
import java.util.List;

import java.util.UUID;
@RestController @RequiredArgsConstructor
public class CartController {
    private final CartService service;
    @PostMapping("/carts") public ResponseEntity<CartDto> create(@Valid @RequestBody CreateCartRequest request) {
        var cart = service.createCart(request.getUserId());
        return ResponseEntity.created(URI.create("/carts/" + cart.getId())).body(cart);
    }
    @GetMapping("/carts/{id}") public CartDto get(@PathVariable UUID id) { return service.getCart(id); }
    @GetMapping("/users/{id}/cart") public CartDto customerCart(@PathVariable Long id) { return service.getCustomerCart(id); }
    // Adding always returns 200, including repeat additions; documented for the UI.
    @PostMapping("/carts/{id}/items") public CartItemDto add(@PathVariable UUID id, @Valid @RequestBody AddItemsToCart request) { return service.addToCart(id, request.getProductId()); }
    @PutMapping("/carts/{id}/items/{productId}") public CartItemDto update(@PathVariable UUID id, @PathVariable Long productId, @Valid @RequestBody UpdateCartItemRequest request) { return service.updateItem(id, productId, request.getQuantity()); }
    @DeleteMapping("/carts/{id}/items/{productId}") public ResponseEntity<Void> remove(@PathVariable UUID id, @PathVariable Long productId) {
        service.removeItem(id, productId);
        return ResponseEntity.noContent().build();
    }
    @DeleteMapping("/carts/{id}/items") public ResponseEntity<Void> clear(@PathVariable UUID id) {
        service.clearCart(id);
        return ResponseEntity.noContent().build();
    }
    @DeleteMapping("/carts/{id}") public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.deleteCart(id);
        return ResponseEntity.noContent().build();
    }
}
