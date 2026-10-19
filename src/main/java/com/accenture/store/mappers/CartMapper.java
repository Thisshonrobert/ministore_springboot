//member 4
package com.accenture.store.mappers;

import com.accenture.store.dtos.CartDto;
import com.accenture.store.dtos.CartItemDto;
import com.accenture.store.dtos.CartProductDto;
import com.accenture.store.entities.Cart;
import com.accenture.store.entities.CartItem;
import com.accenture.store.entities.Product;
import com.accenture.store.entities.User;
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
