//member 1
package com.codewithmosh.store.dtos;

import lombok.Data;
import jakarta.validation.constraints.*;
@Data
public class CategoryRequest {
    @NotBlank @Size(max = 100) private String name;
}
