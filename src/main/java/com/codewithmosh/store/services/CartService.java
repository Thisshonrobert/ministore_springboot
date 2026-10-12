//member 4
package com.codewithmosh.store.services;

import com.codewithmosh.store.entities.*;
import com.codewithmosh.store.dtos.*;
import com.codewithmosh.store.repositories.*;
import com.codewithmosh.store.mappers.*;
import com.codewithmosh.store.exceptions.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

import java.util.UUID;
@Service @RequiredArgsConstructor @Transactional
public class CartService {
    private final CartRepository carts;
    private final UserRepository users;
    private final ProductRepository products;
    private final CartMapper mapper;
    public CartDto createCart(Long userId) {
        var user = users.findById(userId).orElseThrow(() -> new ResourceNotFoundException("Customer not found."));
        if (carts.existsByUserId(userId)) throw new ConflictException("This customer already has a cart. Load it instead.");
        var cart = new Cart();
        cart.setUser(user);
        return mapper.toDto(carts.save(cart));
    }
    @Transactional(readOnly = true)
    public CartDto getCart(UUID id) { return mapper.toDto(find(id)); }
    @Transactional(readOnly = true)
    public CartDto getCustomerCart(Long userId) {
        if (!users.existsById(userId)) throw new ResourceNotFoundException("Customer not found.");
        return mapper.toDto(carts.findByUserId(userId).orElseThrow(() -> new ResourceNotFoundException("This customer has no cart yet.")));
    }
    public CartItemDto addToCart(UUID id, Long productId) {
        var cart = forUpdate(id);
        var product = products.findById(productId).orElseThrow(() -> new ResourceNotFoundException("Product not found."));
        var existing = cart.getItem(productId);
        if (existing != null && existing.getQuantity() >= 100) throw new BusinessRuleException("Quantity must be between 1 and 100.");
        var item = cart.addItem(product);
        carts.save(cart);
        return mapper.toDto(item);
    }
    public CartItemDto updateItem(UUID id, Long productId, Integer quantity) {
        if (quantity == null || quantity < 1 || quantity > 100) throw new BusinessRuleException("Quantity must be between 1 and 100.");
        var item = item(forUpdate(id), productId);
        item.setQuantity(quantity);
        return mapper.toDto(item);
    }
    public void removeItem(UUID id, Long productId) {
        var cart = forUpdate(id);
        item(cart, productId);
        cart.removeItem(productId);
    }
    public void clearCart(UUID id) { forUpdate(id).clear(); }
    public void deleteCart(UUID id) { carts.delete(forUpdate(id)); }
    private Cart forUpdate(UUID id) {
        return carts.findForUpdate(id).orElseThrow(() -> new ResourceNotFoundException("Cart not found."));
    }
    private CartItem item(Cart cart, Long productId) {
        var item = cart.getItem(productId);
        if (item == null) throw new ResourceNotFoundException("Product is not in this cart.");
        return item;
    }
    private Cart find(UUID id) {
        return carts.findById(id).orElseThrow(() -> new ResourceNotFoundException("Cart not found."));
    }
}
