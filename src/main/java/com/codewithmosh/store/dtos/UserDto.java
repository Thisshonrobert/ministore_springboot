//member 3
package com.codewithmosh.store.dtos;

import lombok.Data;
import jakarta.validation.constraints.*;
@Data
public class UserDto {
    private Long id;
    private String name;
    private String email;
}
