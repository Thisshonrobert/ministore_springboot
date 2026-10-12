//member 1
package com.codewithmosh.store.dtos;

import lombok.Data;
import jakarta.validation.constraints.*;
@Data
public class CategoryDto {
    private Byte id;
    private String name;
}
