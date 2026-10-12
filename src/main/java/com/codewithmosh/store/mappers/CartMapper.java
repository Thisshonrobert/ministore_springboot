//member 4
package com.codewithmosh.store.mappers;

import com.codewithmosh.store.entities.*;
import com.codewithmosh.store.dtos.*;
import org.mapstruct.*;
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface CartMapper {
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "totalPrice", expression = "java(cart.getTotalPrice())")
    CartDto toDto(Cart cart);
    @Mapping(target = "totalPrice", expression = "java(item.getTotalPrice())")
    CartItemDto toDto(CartItem item);
    CartProductDto toDto(Product product);
}
