//member 4
package com.accenture.store.exceptions;

/**
 * Raised when the external AI provider is not configured, rejects the request
 * or cannot be reached. It is mapped to HTTP 503 by the global handler.
 */
public class AiUnavailableException extends RuntimeException {
    public AiUnavailableException(String message) {
        super(message);
    }
}
