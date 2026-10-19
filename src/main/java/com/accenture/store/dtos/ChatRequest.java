//member 4
package com.accenture.store.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ChatRequest {
    @NotBlank(message = "Type a question for the shop assistant.")
    @Size(max = 1000, message = "Questions must be 1000 characters or fewer.")
    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
