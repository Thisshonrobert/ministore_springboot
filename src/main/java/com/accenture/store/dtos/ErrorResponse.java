//member 3
package com.accenture.store.dtos;

import java.util.Map;

public record ErrorResponse(String message, Map<String, String> fieldErrors) {
}
