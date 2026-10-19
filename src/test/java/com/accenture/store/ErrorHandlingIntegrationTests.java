//member 3
package com.accenture.store;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Member 3: shared error responses and the removed legacy management routes.
 * The old /users and /carts management endpoints must stay gone (404), and every
 * controller must keep the same JSON error shape.
 */
class ErrorHandlingIntegrationTests extends IntegrationTestSupport {
    @Test void legacyManagementEndpointsAreRemoved() throws Exception {
        startSession();
        call(get("/users"), null, 404);
        call(get("/users/1"), null, 404);
        call(post("/users"), "{\"name\":\"Test\",\"email\":\"legacy@example.com\"}", 404);
        call(delete("/users/1"), null, 404);
        call(get("/carts"), null, 404);
        call(post("/carts"), "{\"userId\":1}", 404);
        call(get("/users/1/cart"), null, 404);
        call(delete("/carts/" + UUID.randomUUID()), null, 404);
    }

    @Test void consistentErrorsAndMalformedRequests() throws Exception {
        startSession();
        customer();
        JsonNode error = call(post("/auth/register"), "{\"name\":\"Test\",\"email\":\"bad\",\"password\":\"short\"}", 400);
        assertThat(error.get("fieldErrors").has("email")).isTrue();
        assertThat(error.has("message")).isTrue();
        assertThat(call(get("/products/999999"), null, 404).get("fieldErrors").isObject()).isTrue();
        call(post("/categories"), "{bad", 400);
        call(post(CART + "/items"), "{}", 400);
        call(post(CART + "/items"), "{\"productId\":999999}", 404);
    }
}
