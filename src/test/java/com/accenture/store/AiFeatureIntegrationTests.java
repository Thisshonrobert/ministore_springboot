//member 3
package com.accenture.store;

import com.accenture.store.ai.AiClient;
import com.accenture.store.exceptions.AiUnavailableException;
import com.accenture.store.services.CsrfTokenService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * The AI features are tested against a fake AiClient, so the suite never needs a
 * live API key and never costs a request. The fake is also what proves the
 * grounding: it captures the exact system instruction and prompt the real
 * client would send, and the tests assert that real database rows are in them.
 */
@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test")
class AiFeatureIntegrationTests {
    private static final String PASSWORD = "StorePass123!";
    private static final String CUSTOMER_NAME = "Review Customer";
    private static final String CHAT_SUMMARY =
            "Most customers liked the paper and the price, but two mentioned a thin cover.";

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    @MockitoBean AiClient aiClient;

    private MockHttpSession session;
    private String csrfToken;

    /** Starts an anonymous session and keeps the CSRF token the interceptors expect. */
    private void startSession() throws Exception {
        MvcResult result = mvc.perform(get("/auth/csrf"))
                .andExpect(status().isOk())
                .andReturn();

        session = (MockHttpSession) result.getRequest().getSession(false);
        csrfToken = json.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }

    private JsonNode call(MockHttpServletRequestBuilder request, String body, int status) throws Exception {
        if (session != null) {
            request.session(session);
        }
        if (csrfToken != null) {
            request.header(CsrfTokenService.HEADER_NAME, csrfToken);
        }
        if (body != null) {
            request.contentType(MediaType.APPLICATION_JSON).content(body);
        }

        MvcResult result = mvc.perform(request)
                .andExpect(status().is(status))
                .andReturn();

        if (session != null) {
            if (session.isInvalid()) {
                session = null;
                csrfToken = null;
            } else {
                Object rotatedToken = session.getAttribute(CsrfTokenService.SESSION_ATTRIBUTE);
                if (rotatedToken instanceof String token) {
                    csrfToken = token;
                }
            }
        }

        String content = result.getResponse().getContentAsString();
        return content.isBlank() ? json.createObjectNode() : json.readTree(content);
    }

    private String unique(String prefix) {
        return prefix + "-" + UUID.randomUUID();
    }

    private String register() throws Exception {
        String email = unique("ai") + "@example.com";
        String body = json.createObjectNode()
                .put("name", CUSTOMER_NAME)
                .put("email", email)
                .put("password", PASSWORD)
                .toString();
        return call(post("/auth/register"), body, 201).get("id").asText();
    }

    private String category() throws Exception {
        return call(post("/categories"), "{\"name\":\"" + unique("Ai Shelf") + "\"}", 201).get("id").asText();
    }

    private String product(String name) throws Exception {
        String categoryId = category();
        String body = json.createObjectNode()
                .put("name", name)
                .put("description", "Smooth paper")
                .put("price", 250)
                .put("categoryId", Long.parseLong(categoryId))
                .toString();
        return call(post("/products"), body, 201).get("id").asText();
    }

    private String reviewJson(String productId, int rating, String comment) {
        return json.createObjectNode()
                .put("productId", Long.parseLong(productId))
                .put("rating", rating)
                .put("comment", comment)
                .toString();
    }

    /** A brand-new signed-in customer reviews the product and is signed out again. */
    private void reviewAsNewCustomer(String productId, int rating, String comment) throws Exception {
        startSession();
        register();
        call(post("/me/reviews"), reviewJson(productId, rating, comment), 201);
        call(post("/auth/logout"), null, 204);
    }

    private boolean containsName(JsonNode products, String name) {
        for (JsonNode product : products) {
            if (name.equals(product.get("name").asText())) {
                return true;
            }
        }
        return false;
    }

    @Test
    void chatAnswersAreGroundedInDatabaseRows() throws Exception {
        startSession();
        String name = unique("Atlas Notebook");
        String productId = product(name);

        when(aiClient.generate(anyString(), anyString()))
                .thenReturn("The " + name + " costs INR 250.00 and sits on the Books shelf.");

        JsonNode response = call(post("/chat"), "{\"message\":\"How much is the " + name + "?\"}", 200);

        // The reply is the model's own text; the backend adds no invented facts.
        assertThat(response.get("reply").asText()).startsWith("The " + name);
        assertThat(containsName(response.get("products"), name)).isTrue();
        assertThat(response.get("products").get(0).get("id").asText()).isEqualTo(productId);
        assertThat(response.get("products").get(0).get("price").decimalValue()).isEqualByComparingTo("250");

        ArgumentCaptor<String> systemInstruction = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> customerQuestion = ArgumentCaptor.forClass(String.class);
        verify(aiClient).generate(systemInstruction.capture(), customerQuestion.capture());

        // The instruction the model actually received contains the row read from MySQL.
        assertThat(systemInstruction.getValue()).contains(name);
        assertThat(systemInstruction.getValue()).contains("250");
        assertThat(systemInstruction.getValue()).contains("Smooth paper");
        // ...together with the rule that stops the model from inventing products.
        assertThat(systemInstruction.getValue()).contains("Never invent a product");
        assertThat(customerQuestion.getValue()).isEqualTo("How much is the " + name + "?");

        // The catalogue is a database read, so the fake product used below is never in it.
        assertThat(systemInstruction.getValue()).doesNotContain(unique("Imaginary Bundle"));
    }

    @Test
    void chatRejectsMissingBlankOversizedAndMalformedMessages() throws Exception {
        startSession();

        JsonNode missing = call(post("/chat"), "{}", 400);
        assertThat(missing.get("fieldErrors").has("message")).isTrue();

        call(post("/chat"), "{\"message\":\"   \"}", 400);
        call(post("/chat"), "{\"message\":\"" + "a".repeat(1001) + "\"}", 400);
        call(post("/chat"), "{ this is not json", 400);
        mvc.perform(get("/chat")).andExpect(status().isMethodNotAllowed());

        verifyNoInteractions(aiClient);
    }

    @Test
    void chatNeedsAMatchingCsrfToken() throws Exception {
        startSession();
        String body = "{\"message\":\"Do you sell notebooks?\"}";

        mvc.perform(post("/chat")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());

        mvc.perform(post("/chat")
                        .session(session)
                        .header(CsrfTokenService.HEADER_NAME, "not-the-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());

        verifyNoInteractions(aiClient);
    }

    @Test
    void chatReportsAnUnavailableAssistantInsteadOfInventingAnAnswer() throws Exception {
        startSession();
        when(aiClient.generate(anyString(), anyString()))
                .thenThrow(new AiUnavailableException("The AI assistant is unavailable right now."));

        JsonNode error = call(post("/chat"), "{\"message\":\"Do you sell notebooks?\"}", 503);

        assertThat(error.get("message").asText()).contains("unavailable");
        assertThat(error.has("fieldErrors")).isTrue();
    }

    @Test
    void submittingAReviewRequiresASessionAndACsrfToken() throws Exception {
        startSession();
        String productId = product(unique("Guarded Notebook"));
        String body = reviewJson(productId, 5, "Trying to review without signing in.");

        mvc.perform(post("/me/reviews")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());

        mvc.perform(post("/me/reviews")
                        .session(session)
                        .header(CsrfTokenService.HEADER_NAME, "not-the-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());

        // A matching CSRF token is not enough on its own: the customer must be signed in.
        JsonNode unsigned = call(post("/me/reviews"), body, 401);
        assertThat(unsigned.get("message").asText()).contains("sign in");

        verifyNoInteractions(aiClient);
    }

    @Test
    void signedInCustomerSubmitsARatingAndWrittenReview() throws Exception {
        startSession();
        String name = unique("Bramble Notebook");
        String productId = product(name);
        register();

        JsonNode review = call(post("/me/reviews"), reviewJson(productId, 5, "  Lovely paper.  "), 201);
        assertThat(review.get("rating").asInt()).isEqualTo(5);
        assertThat(review.get("comment").asText()).isEqualTo("Lovely paper.");
        assertThat(review.get("authorName").asText()).isEqualTo(CUSTOMER_NAME);
        assertThat(review.get("createdAt").asText()).isNotBlank();

        JsonNode listed = call(get("/products/" + productId + "/reviews"), null, 200);
        assertThat(listed.get("productName").asText()).isEqualTo(name);
        assertThat(listed.get("reviewCount").asInt()).isEqualTo(1);
        assertThat(listed.get("averageRating").asDouble()).isEqualTo(5.0);
        assertThat(listed.get("reviews").get(0).get("comment").asText()).isEqualTo("Lovely paper.");
        assertThat(listed.get("summary").isNull()).isTrue();
        // Only the display name is published; the customer's email never leaves the backend.
        assertThat(listed.get("reviews").get(0).has("email")).isFalse();
        assertThat(listed.toString()).doesNotContain("@example.com");

        // Validation and unknown products keep the shared error shape.
        JsonNode invalid = call(post("/me/reviews"), reviewJson(productId, 0, "Zero stars."), 400);
        assertThat(invalid.get("fieldErrors").has("rating")).isTrue();
        call(post("/me/reviews"), reviewJson(productId, 6, "Six stars."), 400);
        call(post("/me/reviews"), "{\"productId\":" + productId + ",\"rating\":5,\"comment\":\"   \"}", 400);
        call(post("/me/reviews"), "{}", 400);
        call(post("/me/reviews"), reviewJson("999999", 5, "No such product."), 404);

        // One review per customer per product, but another customer may add their own.
        call(post("/me/reviews"), reviewJson(productId, 4, "A second opinion."), 409);
        reviewAsNewCustomer(productId, 4, "A second opinion.");
        startSession();
        assertThat(call(get("/products/" + productId + "/reviews"), null, 200).get("reviewCount").asInt()).isEqualTo(2);

        verifyNoInteractions(aiClient);
    }

    @Test
    void productsWithNoReviewsOrTooFewReviewsAreHandledGracefully() throws Exception {
        startSession();
        String productId = product(unique("Cedar Notebook"));

        JsonNode empty = call(get("/products/" + productId + "/reviews"), null, 200);
        assertThat(empty.get("reviews").size()).isZero();
        assertThat(empty.get("reviewCount").asInt()).isZero();
        assertThat(empty.get("averageRating").isNull()).isTrue();
        assertThat(empty.get("summary").isNull()).isTrue();
        assertThat(empty.get("summaryMessage").isNull()).isTrue();

        JsonNode noReviews = call(post("/products/" + productId + "/reviews/summary"), null, 200);
        assertThat(noReviews.get("summary").isNull()).isTrue();
        assertThat(noReviews.get("summaryMessage").asText()).contains("no reviews");

        // Two reviews are still below the configured minimum of three.
        reviewAsNewCustomer(productId, 5, "Good paper.");
        reviewAsNewCustomer(productId, 2, "The cover bent in my bag.");
        startSession();

        JsonNode tooFew = call(post("/products/" + productId + "/reviews/summary"), null, 200);
        assertThat(tooFew.get("summary").isNull()).isTrue();
        assertThat(tooFew.get("reviewCount").asInt()).isEqualTo(2);
        assertThat(tooFew.get("summaryMessage").asText()).contains("2 reviews");

        // The AI is never asked for a summary it cannot write.
        verifyNoInteractions(aiClient);
    }

    @Test
    void summaryIsGeneratedFromRealReviewsCachedAndInvalidatedByANewReview() throws Exception {
        String name = unique("Dune Notebook");
        startSession();
        String productId = product(name);

        reviewAsNewCustomer(productId, 5, "Lovely paper and quick delivery to my desk.");
        reviewAsNewCustomer(productId, 5, "Great value for the price.");
        reviewAsNewCustomer(productId, 4, "Good paper, but the cover is thin.");

        String summaryPath = "/products/" + productId + "/reviews/summary";
        startSession();
        assertThat(call(get("/products/" + productId + "/reviews"), null, 200).get("summary").isNull()).isTrue();

        when(aiClient.generate(anyString(), anyString())).thenReturn(CHAT_SUMMARY);

        JsonNode summarized = call(post(summaryPath), null, 200);
        assertThat(summarized.get("summary").get("content").asText()).isEqualTo(CHAT_SUMMARY);
        assertThat(summarized.get("summary").get("generatedAt").asText()).isNotBlank();
        assertThat(summarized.get("summary").get("expiresAt").asText()).isNotBlank();
        assertThat(summarized.get("reviewCount").asInt()).isEqualTo(3);
        assertThat(summarized.get("averageRating").asDouble()).isEqualTo(4.7);
        assertThat(summarized.get("summaryMessage").isNull()).isTrue();

        ArgumentCaptor<String> summaryInstruction = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> reviewsText = ArgumentCaptor.forClass(String.class);
        verify(aiClient).generate(summaryInstruction.capture(), reviewsText.capture());

        // The AI summarised the real reviews, and no customer identity was sent with them.
        assertThat(reviewsText.getValue()).contains(name);
        assertThat(reviewsText.getValue()).contains("Reviews in total: 3");
        assertThat(reviewsText.getValue()).contains("Lovely paper and quick delivery to my desk.");
        assertThat(reviewsText.getValue()).contains("Good paper, but the cover is thin.");
        assertThat(reviewsText.getValue()).doesNotContain(CUSTOMER_NAME);
        assertThat(reviewsText.getValue()).doesNotContain("@example.com");

        // The summary is cached, so reading reviews and asking again both reuse it.
        assertThat(call(get("/products/" + productId + "/reviews"), null, 200)
                .get("summary").get("content").asText()).isEqualTo(CHAT_SUMMARY);
        assertThat(call(post(summaryPath), null, 200)
                .get("summary").get("content").asText()).isEqualTo(CHAT_SUMMARY);
        verify(aiClient, times(1)).generate(anyString(), anyString());

        // A new review invalidates the cached summary, which is then regenerated.
        reviewAsNewCustomer(productId, 1, "Mine arrived with a damaged corner.");
        startSession();

        JsonNode afterNewReview = call(get("/products/" + productId + "/reviews"), null, 200);
        assertThat(afterNewReview.get("summary").isNull()).isTrue();
        assertThat(afterNewReview.get("reviewCount").asInt()).isEqualTo(4);

        when(aiClient.generate(anyString(), anyString())).thenReturn("Updated summary after the fourth review.");
        JsonNode refreshed = call(post(summaryPath), null, 200);
        assertThat(refreshed.get("summary").get("content").asText())
                .isEqualTo("Updated summary after the fourth review.");
        verify(aiClient, times(2)).generate(anyString(), anyString());
    }

    @Test
    void reviewsAndSummariesForUnknownOrMalformedProductsFailCleanly() throws Exception {
        startSession();

        call(get("/products/999999/reviews"), null, 404);
        call(post("/products/999999/reviews/summary"), null, 404);
        call(get("/products/not-a-number/reviews"), null, 400);
        call(post("/products/not-a-number/reviews/summary"), null, 400);

        verifyNoInteractions(aiClient);
    }
}
