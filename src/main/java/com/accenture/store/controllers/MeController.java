//member 4
package com.codewithmosh.store.controllers;

import com.codewithmosh.store.dtos.AddItemsToCart;
import com.codewithmosh.store.dtos.CartDto;
import com.codewithmosh.store.dtos.CartItemDto;
import com.codewithmosh.store.dtos.UpdateCartItemRequest;
import com.codewithmosh.store.services.AuthService;
import com.codewithmosh.store.services.CartService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.UUID;

/**
 * Customer-facing cart API. The customer is always taken from the HttpSession,
 * never from a path variable or request body.
 */
@RestController
@RequestMapping("/me")
public class MeController {
    private final CartService cartService;

    public MeController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping("/cart")
    public CartDto getCart(HttpSession session) {
        return cartService.getCustomerCart(currentCustomerId(session));
    }

    @PostMapping("/cart")
    public ResponseEntity<CartDto> createCart(HttpSession session) {
        CartDto cart = cartService.createCart(currentCustomerId(session));
        return ResponseEntity.created(URI.create("/me/cart")).body(cart);
    }

    @PostMapping("/cart/items")
    public CartItemDto addItem(HttpSession session, @Valid @RequestBody AddItemsToCart request) {
        return cartService.addToCart(currentCartId(session), request.getProductId());
    }

    @PutMapping("/cart/items/{productId}")
    public CartItemDto updateItem(HttpSession session,
                                  @PathVariable Long productId,
                                  @Valid @RequestBody UpdateCartItemRequest request) {
        return cartService.updateItem(currentCartId(session), productId, request.getQuantity());
    }

    @DeleteMapping("/cart/items/{productId}")
    public ResponseEntity<Void> removeItem(HttpSession session, @PathVariable Long productId) {
        cartService.removeItem(currentCartId(session), productId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/cart/items")
    public ResponseEntity<Void> clearCart(HttpSession session) {
        cartService.clearCart(currentCartId(session));
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/cart")
    public ResponseEntity<Void> deleteCart(HttpSession session) {
        cartService.deleteCart(currentCartId(session));
        return ResponseEntity.noContent().build();
    }

    private Long currentCustomerId(HttpSession session) {
        return (Long) session.getAttribute(AuthService.SESSION_CUSTOMER_ID);
    }

    private UUID currentCartId(HttpSession session) {
        return cartService.getCustomerCart(currentCustomerId(session)).getId();
    }
}
