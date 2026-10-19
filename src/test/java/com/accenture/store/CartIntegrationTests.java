//member 3
package com.accenture.store;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

/**
 * Member 3: the signed-in customer's cart (the /me cart slice). The customer
 * always comes from the session, quantities are limited, and concurrent adds
 * keep every increment thanks to the pessimistic row lock.
 */
class CartIntegrationTests extends IntegrationTestSupport {
    @Test void sessionCustomerCartLifecycle() throws Exception {
        startSession();
        String customerId = customer();

        JsonNode cart = call(get(CART), null, 200);
        assertThat(cart.get("userId").asText()).isEqualTo(customerId);
        assertThat(cart.get("items").size()).isZero();

        call(delete(CART), null, 204);
        call(get(CART), null, 404);

        call(post(CART), null, 201);
        assertThat(call(get(CART), null, 200).get("userId").asText()).isEqualTo(customerId);

        // A customer has at most one cart; registration already created it and creation cannot repeat.
        call(post(CART), null, 409);
    }

    @Test void cartItemsPersistAndTotalsAndClear() throws Exception {
        startSession();
        customer();
        String category = category(), p = product(category), p2 = product(category);
        String body = "{\"productId\":" + p + "}";
        call(post(CART + "/items"), body, 200);
        call(post(CART + "/items"), body, 200);
        JsonNode view = call(get(CART), null, 200);
        assertThat(view.get("items").size()).isEqualTo(1);
        assertThat(view.get("items").get(0).get("quantity").asInt()).isEqualTo(2);
        assertThat(view.get("totalPrice").decimalValue()).isEqualByComparingTo("500");
        call(put(CART + "/items/" + p), "{\"quantity\":3}", 200);
        assertThat(call(get(CART), null, 200).get("totalPrice").decimalValue()).isEqualByComparingTo("750");
        call(delete("/products/" + p), null, 409);
        call(delete(CART + "/items/" + p), null, 204);
        assertThat(call(get(CART), null, 200).get("items").size()).isZero();
        call(post(CART + "/items"), "{\"productId\":" + p2 + "}", 200);
        call(delete(CART + "/items"), null, 204);
        assertThat(call(get(CART), null, 200).get("totalPrice").decimalValue()).isEqualByComparingTo("0");
        call(post(CART + "/items"), body, 200);
        call(delete(CART), null, 204);
        call(get("/products/" + p), null, 200);
        call(delete("/products/" + p), null, 204);
    }

    @Test void quantityLimitAndMissingItems() throws Exception {
        startSession();
        customer();
        String p = product(category()), path = CART + "/items/" + p;
        call(put(path), "{\"quantity\":2}", 404);
        call(post(CART + "/items"), "{\"productId\":" + p + "}", 200);
        call(put(path), "{\"quantity\":0}", 400);
        call(put(path), "{\"quantity\":101}", 400);
        call(put(path), "{\"quantity\":100}", 200);
        call(post(CART + "/items"), "{\"productId\":" + p + "}", 400);
        assertThat(call(get(CART), null, 200).get("items").get(0).get("quantity").asInt()).isEqualTo(100);
        call(delete(CART + "/items/999999"), null, 404);
    }

    @Test void fractionalQuantityIsRejected() throws Exception {
        startSession();
        customer();
        String p = product(category());
        call(post(CART + "/items"), "{\"productId\":" + p + "}", 200);
        call(put(CART + "/items/" + p), "{\"quantity\":1.9}", 400);
    }

    @Test void concurrentAddsKeepEveryIncrement() throws Exception {
        startSession();
        customer();
        String p = product(category());
        String body = "{\"productId\":" + p + "}";
        call(post(CART + "/items"), body, 200);

        var workers = Executors.newFixedThreadPool(4);
        var start = new CountDownLatch(1);
        try {
            var futures = new ArrayList<Future<?>>();
            for (int i = 0; i < 4; i++) futures.add(workers.submit(() -> {
                start.await();
                call(post(CART + "/items"), body, 200);
                return null;
            }));
            start.countDown();
            for (var future : futures) future.get(15, TimeUnit.SECONDS);
            assertThat(call(get(CART), null, 200).get("items").get(0).get("quantity").asInt()).isEqualTo(5);
        } finally {
            workers.shutdownNow();
        }
    }
}
