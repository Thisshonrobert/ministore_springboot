//member 4
package com.codewithmosh.store.dtos;

import lombok.Data;
import jakarta.validation.constraints.*;
@Data
public class CreateCartRequest {
    @NotNull @Positive private Long userId;
}
