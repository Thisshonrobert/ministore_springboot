//member 4
package com.codewithmosh.store.controllers;

import com.codewithmosh.store.dtos.AddItemsToCart;
import com.codewithmosh.store.dtos.CartDto;
import com.codewithmosh.store.dtos.CartItemDto;
import com.codewithmosh.store.dtos.CreateCartRequest;
import com.codewithmosh.store.dtos.UpdateCartItemRequest;
import com.codewithmosh.store.services.CartService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.UUID;

@RestController
public class CartController {
    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping("/carts")
    public ResponseEntity<CartDto> create(@Valid @RequestBody CreateCartRequest request) {
        CartDto cart = cartService.createCart(request.getUserId());
        URI location = URI.create("/carts/" + cart.getId());
        return ResponseEntity.created(location).body(cart);
    }

    @GetMapping("/carts/{id}")
    public CartDto get(@PathVariable UUID id) {
        return cartService.getCart(id);
    }

    @GetMapping("/users/{id}/cart")
    public CartDto customerCart(@PathVariable Long id) {
        return cartService.getCustomerCart(id);
    }

    // Adding always returns 200, including repeat additions; documented for the UI.
    @PostMapping("/carts/{id}/items")
    public CartItemDto add(@PathVariable UUID id, @Valid @RequestBody AddItemsToCart request) {
        return cartService.addToCart(id, request.getProductId());
    }

    @PutMapping("/carts/{id}/items/{productId}")
    public CartItemDto update(@PathVariable UUID id,
                              @PathVariable Long productId,
                              @Valid @RequestBody UpdateCartItemRequest request) {
        return cartService.updateItem(id, productId, request.getQuantity());
    }

    @DeleteMapping("/carts/{id}/items/{productId}")
    public ResponseEntity<Void> remove(@PathVariable UUID id, @PathVariable Long productId) {
        cartService.removeItem(id, productId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/carts/{id}/items")
    public ResponseEntity<Void> clear(@PathVariable UUID id) {
        cartService.clearCart(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/carts/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        cartService.deleteCart(id);
        return ResponseEntity.noContent().build();
    }
}
