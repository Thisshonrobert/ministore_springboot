//member 4
package com.codewithmosh.store.dtos;

import lombok.Data;
import jakarta.validation.constraints.*;
@Data
public class CartItemDto {
    private CartProductDto product;
    private int quantity;
    private java.math.BigDecimal totalPrice;
}
