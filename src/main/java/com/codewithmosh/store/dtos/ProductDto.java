//member 2
package com.codewithmosh.store.dtos;

import lombok.Data;
import jakarta.validation.constraints.*;
@Data
public class ProductDto {
    private Long id;
    private String name;
    private String description;
    private java.math.BigDecimal price;
    private Byte categoryId;
}
