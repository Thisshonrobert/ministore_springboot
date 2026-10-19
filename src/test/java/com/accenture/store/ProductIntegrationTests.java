//member 2
package com.accenture.store;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

/**
 * Member 2: the product slice. Mapping to a category ID, the category filter,
 * updates, and price/category validation on create.
 */
class ProductIntegrationTests extends IntegrationTestSupport {
    @Test void productMappingFilteringUpdateAndValidation() throws Exception {
        String c = category(), other = category(), p = product(c);
        JsonNode products = call(get("/products").param("categoryId", c), null, 200);
        assertThat(products.toString()).contains("Notebook");
        call(put("/products/" + p), "{\"name\":\"Pen\",\"description\":\"Blue\",\"price\":100,\"categoryId\":" + other + "}", 200);
        assertThat(call(get("/products/" + p), null, 200).get("categoryId").asText()).isEqualTo(other);
        call(post("/products"), "{\"name\":\"Bad\",\"price\":0,\"categoryId\":" + c + "}", 400);
        call(post("/products"), "{\"name\":\"Bad\",\"price\":1,\"categoryId\":127}", 404);
    }
}
