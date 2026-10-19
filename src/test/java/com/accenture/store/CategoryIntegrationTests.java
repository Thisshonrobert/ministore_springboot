//member 1
package com.accenture.store;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

/**
 * Member 1: the category slice. Category CRUD, and the rule that a category
 * still used by a product cannot be deleted.
 */
class CategoryIntegrationTests extends IntegrationTestSupport {
    @Test void categoryCrudAndOccupiedDeletion() throws Exception {
        String c = category();
        call(put("/categories/" + c), "{\"name\":\"Stationery\"}", 200);
        assertThat(call(get("/categories/" + c), null, 200).get("name").asText()).isEqualTo("Stationery");
        String p = product(c);
        call(delete("/categories/" + c), null, 409);
        call(delete("/products/" + p), null, 204);
        call(delete("/categories/" + c), null, 204);
        call(get("/categories/" + c), null, 404);
    }
}
