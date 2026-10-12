//member 4
package com.codewithmosh.store.dtos;

import lombok.Data;
import jakarta.validation.constraints.*;
@Data
public class UpdateCartItemRequest {
    @NotNull @Min(1) @Max(100) private Integer quantity;
}
