//member 4
package com.codewithmosh.store.mappers;

import com.codewithmosh.store.dtos.CartDto;
import com.codewithmosh.store.dtos.CartItemDto;
import com.codewithmosh.store.dtos.CartProductDto;
import com.codewithmosh.store.entities.Cart;
import com.codewithmosh.store.entities.CartItem;
import com.codewithmosh.store.entities.Product;
import com.codewithmosh.store.entities.User;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class CartMapper {
    public CartDto toCartDto(Cart cart) {
        if (cart == null) {
            return null;
        }

        CartDto dto = new CartDto();
        dto.setId(cart.getId());

        User user = cart.getUser();
        if (user != null) {
            dto.setUserId(user.getId());
        }

        Set<CartItem> cartItems = cart.getItems();
        if (cartItems != null) {
            List<CartItemDto> items = new ArrayList<>();
            for (CartItem item : cartItems) {
                items.add(toCartItemDto(item));
            }
            dto.setItems(items);
        }

        dto.setTotalPrice(cart.getTotalPrice());
        return dto;
    }

    public CartItemDto toCartItemDto(CartItem item) {
        if (item == null) {
            return null;
        }

        CartItemDto dto = new CartItemDto();
        dto.setProduct(toProductDto(item.getProduct()));
        if (item.getQuantity() != null) {
            dto.setQuantity(item.getQuantity());
        }
        dto.setTotalPrice(item.getTotalPrice());
        return dto;
    }

    public CartProductDto toProductDto(Product product) {
        if (product == null) {
            return null;
        }

        CartProductDto dto = new CartProductDto();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setPrice(product.getPrice());
        return dto;
    }
}
