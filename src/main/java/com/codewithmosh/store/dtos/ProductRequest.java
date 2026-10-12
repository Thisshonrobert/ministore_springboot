//member 2
package com.codewithmosh.store.dtos;

import lombok.Data;
import jakarta.validation.constraints.*;
@Data
public class ProductRequest {
    @NotBlank @Size(max = 100) private String name;
    @Size(max = 1000) private String description;
    @NotNull @DecimalMin(value = "0.0", inclusive = false) @Digits(integer = 8, fraction = 2) private java.math.BigDecimal price;
    @NotNull @Positive private Byte categoryId;
}
