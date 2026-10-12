//member 4
package com.codewithmosh.store.dtos;

import lombok.Data;
import jakarta.validation.constraints.*;
@Data
public class AddItemsToCart {
    @NotNull @Positive private Long productId;
}
