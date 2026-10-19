//member 4
package com.accenture.store.services;

import com.accenture.store.dtos.CartDto;
import com.accenture.store.dtos.CartItemDto;
import com.accenture.store.entities.Cart;
import com.accenture.store.entities.CartItem;
import com.accenture.store.entities.Product;
import com.accenture.store.entities.User;
import com.accenture.store.exceptions.BusinessRuleException;
import com.accenture.store.exceptions.ConflictException;
import com.accenture.store.exceptions.ResourceNotFoundException;
import com.accenture.store.mappers.CartMapper;
import com.accenture.store.repositories.CartRepository;
import com.accenture.store.repositories.ProductRepository;
import com.accenture.store.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class CartService {
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CartMapper cartMapper;

    public CartService(CartRepository cartRepository,
                       UserRepository userRepository,
                       ProductRepository productRepository,
                       CartMapper cartMapper) {
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.cartMapper = cartMapper;
    }

    public CartDto createCart(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found."));

        if (cartRepository.existsByUserId(userId)) {
            throw new ConflictException("This customer already has a cart. Load it instead.");
        }

        Cart cart = new Cart();
        cart.setUser(user);
        Cart savedCart = cartRepository.save(cart);
        return cartMapper.toCartDto(savedCart);
    }

    @Transactional(readOnly = true)
    public CartDto getCustomerCart(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("Customer not found.");
        }

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("This customer has no cart yet."));
        return cartMapper.toCartDto(cart);
    }

    public CartItemDto addToCart(UUID id, Long productId) {
        Cart cart = findCartForUpdate(id);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found."));

        CartItem existingItem = cart.getItem(productId);
        if (existingItem != null && existingItem.getQuantity() >= 100) {
            throw new BusinessRuleException("Quantity must be between 1 and 100.");
        }

        CartItem item = cart.addItem(product);
        cartRepository.save(cart);
        return cartMapper.toCartItemDto(item);
    }

    public CartItemDto updateItem(UUID id, Long productId, Integer quantity) {
        if (quantity == null || quantity < 1 || quantity > 100) {
            throw new BusinessRuleException("Quantity must be between 1 and 100.");
        }

        Cart cart = findCartForUpdate(id);
        CartItem item = findCartItem(cart, productId);
        item.setQuantity(quantity);
        return cartMapper.toCartItemDto(item);
    }

    public void removeItem(UUID id, Long productId) {
        Cart cart = findCartForUpdate(id);
        findCartItem(cart, productId);
        cart.removeItem(productId);
    }

    public void clearCart(UUID id) {
        Cart cart = findCartForUpdate(id);
        cart.clear();
    }

    public void deleteCart(UUID id) {
        Cart cart = findCartForUpdate(id);
        cartRepository.delete(cart);
    }

    private Cart findCartForUpdate(UUID id) {
        return cartRepository.findForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found."));
    }

    private CartItem findCartItem(Cart cart, Long productId) {
        CartItem item = cart.getItem(productId);

        if (item == null) {
            throw new ResourceNotFoundException("Product is not in this cart.");
        }

        return item;
    }
}
