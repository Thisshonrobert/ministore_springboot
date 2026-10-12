//member 4
package com.codewithmosh.store;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.http.MediaType;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test")
class StoreIntegrationTests {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    JsonNode call(MockHttpServletRequestBuilder request, String body, int status) throws Exception {
        if (body != null) request.contentType(MediaType.APPLICATION_JSON).content(body);
        String result = mvc.perform(request).andExpect(status().is(status)).andReturn().getResponse().getContentAsString();
        return result.isBlank() ? json.createObjectNode() : json.readTree(result);
    }
    String user() throws Exception {
        return call(post("/users"), "{\"name\":\"Customer\",\"email\":\"" + java.util.UUID.randomUUID() + "@example.com\"}", 201).get("id").asText();
    }
    String category() throws Exception {
        return call(post("/categories"), "{\"name\":\"Books\"}", 201).get("id").asText();
    }
    String product(String categoryId) throws Exception {
        return call(post("/products"), "{\"name\":\"Notebook\",\"description\":\"Paper\",\"price\":250,\"categoryId\":" + categoryId + "}", 201).get("id").asText();
    }
    String cart(String userId) throws Exception {
        return call(post("/carts"), "{\"userId\":" + userId + "}", 201).get("id").asText();
    }
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
    @Test void productMappingFilteringUpdateAndValidation() throws Exception {
        String c = category(), other = category(), p = product(c);
        JsonNode products = call(get("/products").param("categoryId", c), null, 200);
        assertThat(products.toString()).contains("Notebook");
        call(put("/products/" + p), "{\"name\":\"Pen\",\"description\":\"Blue\",\"price\":100,\"categoryId\":" + other + "}", 200);
        assertThat(call(get("/products/" + p), null, 200).get("categoryId").asText()).isEqualTo(other);
        call(post("/products"), "{\"name\":\"Bad\",\"price\":0,\"categoryId\":" + c + "}", 400);
        call(post("/products"), "{\"name\":\"Bad\",\"price\":1,\"categoryId\":127}", 404);
    }
    @Test void customerUniqueEmailOnCreateAndUpdate() throws Exception {
        String u = user(), other = user();
        String email = call(get("/users/" + u), null, 200).get("email").asText();
        String body = "{\"name\":\"Renamed\",\"email\":\"" + email + "\"}";
        call(put("/users/" + u), body, 200);
        call(post("/users"), body, 409);
        call(put("/users/" + other), body, 409);
        call(delete("/users/" + u), null, 204);
        call(get("/users/" + u), null, 404);
    }
    @Test void customerCartAssociationAndDeletionRules() throws Exception {
        String u = user();
        call(get("/users/" + u + "/cart"), null, 404);
        String c = cart(u);
        assertThat(call(get("/users/" + u + "/cart"), null, 200).get("id").asText()).isEqualTo(c);
        assertThat(call(get("/carts/" + c), null, 200).get("userId").asText()).isEqualTo(u);
        call(post("/carts"), "{\"userId\":" + u + "}", 409);
        call(delete("/users/" + u), null, 409);
        call(delete("/carts/" + c), null, 204);
        call(get("/users/" + u), null, 200);
        call(delete("/users/" + u), null, 204);
    }
    @Test void cartItemsPersistAndTotalsAndClear() throws Exception {
        String u = user(), c = cart(u), category = category(), p = product(category), p2 = product(category);
        String path = "/carts/" + c;
        String body = "{\"productId\":" + p + "}";
        call(post(path + "/items"), body, 200);
        call(post(path + "/items"), body, 200);
        JsonNode view = call(get(path), null, 200);
        assertThat(view.get("items").size()).isEqualTo(1);
        assertThat(view.get("items").get(0).get("quantity").asInt()).isEqualTo(2);
        assertThat(view.get("totalPrice").decimalValue()).isEqualByComparingTo("500");
        call(put(path + "/items/" + p), "{\"quantity\":3}", 200);
        assertThat(call(get(path), null, 200).get("totalPrice").decimalValue()).isEqualByComparingTo("750");
        call(delete("/products/" + p), null, 409);
        call(delete(path + "/items/" + p), null, 204);
        assertThat(call(get(path), null, 200).get("items").size()).isZero();
        call(post(path + "/items"), "{\"productId\":" + p2 + "}", 200);
        call(delete(path + "/items"), null, 204);
        assertThat(call(get(path), null, 200).get("totalPrice").decimalValue()).isEqualByComparingTo("0");
        call(post(path + "/items"), body, 200);
        call(delete(path), null, 204);
        call(get("/products/" + p), null, 200);
        call(delete("/products/" + p), null, 204);
    }
    @Test void quantityLimitAndMissingItems() throws Exception {
        String c = cart(user()), p = product(category()), path = "/carts/" + c + "/items/" + p;
        call(put(path), "{\"quantity\":2}", 404);
        call(post("/carts/" + c + "/items"), "{\"productId\":" + p + "}", 200);
        call(put(path), "{\"quantity\":0}", 400);
        call(put(path), "{\"quantity\":101}", 400);
        call(put(path), "{\"quantity\":100}", 200);
        call(post("/carts/" + c + "/items"), "{\"productId\":" + p + "}", 400);
        assertThat(call(get("/carts/" + c), null, 200).get("items").get(0).get("quantity").asInt()).isEqualTo(100);
        call(delete("/carts/" + c + "/items/999999"), null, 404);
    }
    @Test void consistentErrorsAndMalformedRequests() throws Exception {
        JsonNode error = call(post("/users"), "{\"name\":\"Test\",\"email\":\"bad\"}", 400);
        assertThat(error.get("fieldErrors").has("email")).isTrue();
        assertThat(error.has("message")).isTrue();
        assertThat(call(get("/products/999999"), null, 404).get("fieldErrors").isObject()).isTrue();
        call(get("/carts/not-a-uuid"), null, 400);
        call(post("/categories"), "{bad", 400);
        call(post("/carts"), "{}", 400);
        call(post("/carts"), "{\"userId\":999999}", 404);
        String c = cart(user());
        call(post("/carts/" + c + "/items"), "{}", 400);
        call(post("/carts/" + c + "/items"), "{\"productId\":999999}", 404);
        call(get("/users/999999/cart"), null, 404);
    }
    @Test void fractionalQuantityIsRejected() throws Exception {
        String c = cart(user()), p = product(category());
        call(post("/carts/" + c + "/items"), "{\"productId\":" + p + "}", 200);
        call(put("/carts/" + c + "/items/" + p), "{\"quantity\":1.9}", 400);
    }
    @Test void concurrentAddsKeepEveryIncrement() throws Exception {
        String c = cart(user()), p = product(category()), path = "/carts/" + c + "/items";
        String body = "{\"productId\":" + p + "}";
        call(post(path), body, 200);
        var workers = java.util.concurrent.Executors.newFixedThreadPool(4);
        var start = new java.util.concurrent.CountDownLatch(1);
        try {
            var futures = new java.util.ArrayList<java.util.concurrent.Future<?>>();
            for (int i = 0; i < 4; i++) futures.add(workers.submit(() -> {
                start.await(); call(post(path), body, 200); return null;
            }));
            start.countDown();
            for (var future : futures) future.get(15, java.util.concurrent.TimeUnit.SECONDS);
            assertThat(call(get("/carts/" + c), null, 200).get("items").get(0).get("quantity").asInt()).isEqualTo(5);
        } finally { workers.shutdownNow(); }
    }
    @Test void pagesAreServedWithoutLogin() throws Exception {
        for (String page : new String[]{"categories", "products", "customers", "cart"})
            mvc.perform(get("/ui/" + page)).andExpect(status().isOk()).andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML));
    }
}
