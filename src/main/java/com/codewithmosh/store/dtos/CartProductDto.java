//member 4
package com.codewithmosh.store.dtos;

import lombok.Data;
import jakarta.validation.constraints.*;
@Data
public class CartProductDto {
    private Long id;
    private String name;
    private java.math.BigDecimal price;
}
