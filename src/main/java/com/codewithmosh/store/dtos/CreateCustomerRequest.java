//member 3
package com.codewithmosh.store.dtos;

import lombok.Data;
import jakarta.validation.constraints.*;
@Data
public class CreateCustomerRequest {
    @NotBlank @Size(max = 100) private String name;
    @NotBlank @Email @Size(max = 255) private String email;
}
