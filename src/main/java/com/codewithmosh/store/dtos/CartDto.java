//member 4
package com.codewithmosh.store.dtos;

import lombok.Data;
import jakarta.validation.constraints.*;
@Data
public class CartDto {
    private java.util.UUID id;
    private Long userId;
    private java.util.List<CartItemDto> items;
    private java.math.BigDecimal totalPrice;
}
