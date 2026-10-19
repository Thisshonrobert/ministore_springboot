//member 3
package com.accenture.store;

import com.accenture.store.entities.User;
import com.accenture.store.repositories.UserRepository;
import com.accenture.store.services.AuthService;
import com.accenture.store.services.CsrfTokenService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CustomerAuthIntegrationTests {
    private static final String PASSWORD = "StorePass123!";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper json;

    @Autowired
    private UserRepository userRepository;

    private MockHttpSession session;
    private String csrfToken;

    private void startSession() throws Exception {
        MvcResult result = mvc.perform(get("/auth/csrf"))
                .andExpect(status().isOk())
                .andReturn();

        session = (MockHttpSession) result.getRequest().getSession(false);
        csrfToken = json.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }

    private JsonNode call(MockHttpServletRequestBuilder request, String body, int expectedStatus) throws Exception {
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
                .andExpect(status().is(expectedStatus))
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

    private String userJson(String name, String email, String password) {
        return json.createObjectNode()
                .put("name", name)
                .put("email", email)
                .put("password", password)
                .toString();
    }

    private String loginJson(String email, String password) {
        return json.createObjectNode()
                .put("email", email)
                .put("password", password)
                .toString();
    }

    private String uniqueEmail() {
        return "auth-" + UUID.randomUUID() + "@example.com";
    }

    private JsonNode register(String email) throws Exception {
        return call(post("/auth/register"), userJson("Session Customer", email, PASSWORD), 201);
    }

    private String createCategory() throws Exception {
        return call(post("/categories"), "{\"name\":\"Auth Books\"}", 201).get("id").asText();
    }

    private String createProduct(String categoryId) throws Exception {
        return call(post("/products"),
                "{\"name\":\"Session Notebook\",\"description\":\"Paper\",\"price\":250,\"categoryId\":"
                        + categoryId + "}", 201).get("id").asText();
    }

    @Test
    void registrationHashesPasswordRotatesSessionAndAuthenticates() throws Exception {
        startSession();
        String oldSessionId = session.getId();
        String email = uniqueEmail();

        JsonNode customer = call(post("/auth/register"), userJson("Session Customer", email, PASSWORD), 201);

        assertThat(customer.get("email").asText()).isEqualTo(email);
        assertThat(customer.has("password")).isFalse();
        assertThat(customer.has("passwordHash")).isFalse();
        assertThat(session.getId()).isNotEqualTo(oldSessionId);

        User stored = userRepository.findByEmail(email).orElseThrow();
        assertThat(stored.getPasswordHash()).startsWith("pbkdf2_sha256$");
        assertThat(stored.getPasswordHash()).doesNotContain(PASSWORD);

        JsonNode current = call(get("/auth/me"), null, 200);
        assertThat(current.get("id").asLong()).isEqualTo(customer.get("id").asLong());
        assertThat(current.get("email").asText()).isEqualTo(email);
        assertThat(current.has("passwordHash")).isFalse();

        call(post("/auth/logout"), null, 204);
        session = null;
        csrfToken = null;
        call(get("/auth/me"), null, 401);
    }

    @Test
    void duplicateRegistrationIsRejectedCaseInsensitively() throws Exception {
        startSession();
        String email = uniqueEmail();
        register(email);
        call(post("/auth/logout"), null, 204);

        startSession();
        JsonNode error = call(post("/auth/register"), userJson("Another", email.toUpperCase(), PASSWORD), 409);
        assertThat(error.get("message").asText()).contains("Email");
    }

    @Test
    void registrationValidatesAllFields() throws Exception {
        startSession();
        JsonNode error = call(post("/auth/register"),
                "{\"name\":\"\",\"email\":\"not-an-email\",\"password\":\"short\"}", 400);

        assertThat(error.get("fieldErrors").has("name")).isTrue();
        assertThat(error.get("fieldErrors").has("email")).isTrue();
        assertThat(error.get("fieldErrors").has("password")).isTrue();
    }

    @Test
    void validAndInvalidLoginBehaveConsistently() throws Exception {
        startSession();
        String email = uniqueEmail();
        register(email);
        call(post("/auth/logout"), null, 204);

        startSession();
        JsonNode wrongPassword = call(post("/auth/login"), loginJson(email, "WrongPass123!"), 401);
        assertThat(wrongPassword.get("message").asText()).isEqualTo("Invalid email or password.");

        call(post("/auth/login"), loginJson(uniqueEmail(), PASSWORD), 401);

        JsonNode customer = call(post("/auth/login"), loginJson(email, PASSWORD), 200);
        assertThat(customer.get("email").asText()).isEqualTo(email);
        assertThat(customer.has("passwordHash")).isFalse();
        assertThat(call(get("/auth/me"), null, 200).get("email").asText()).isEqualTo(email);

        // The session survives repeated requests, like a page refresh.
        assertThat(call(get("/auth/me"), null, 200).get("id").asLong()).isEqualTo(customer.get("id").asLong());
    }

    @Test
    void mutationWithoutAValidCsrfTokenIsForbidden() throws Exception {
        startSession();
        String email = uniqueEmail();
        String body = userJson("No Token", email, PASSWORD);

        mvc.perform(post("/auth/register")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());

        mvc.perform(post("/auth/register")
                        .session(session)
                        .header(CsrfTokenService.HEADER_NAME, "not-the-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    void customerCartRequiresASessionAndUsesTheSessionCustomer() throws Exception {
        mvc.perform(get("/me/cart")).andExpect(status().isUnauthorized());

        startSession();
        JsonNode customer = register(uniqueEmail());
        long customerId = customer.get("id").asLong();

        JsonNode cart = call(get("/me/cart"), null, 200);
        assertThat(cart.get("userId").asLong()).isEqualTo(customerId);

        String categoryId = createCategory();
        String productId = createProduct(categoryId);
        call(post("/me/cart/items"), "{\"productId\":" + productId + "}", 200);

        JsonNode updated = call(get("/me/cart"), null, 200);
        assertThat(updated.get("items").size()).isEqualTo(1);
        assertThat(updated.get("items").get(0).get("product").get("id").asLong())
                .isEqualTo(Long.parseLong(productId));
        assertThat(updated.get("totalPrice").decimalValue()).isEqualByComparingTo("250");

        call(post("/auth/logout"), null, 204);
        session = null;
        csrfToken = null;
        mvc.perform(get("/me/cart")).andExpect(status().isUnauthorized());
    }

    @Test
    void oneCustomersCartIsNotVisibleToAnotherSession() throws Exception {
        startSession();
        JsonNode first = register(uniqueEmail());
        JsonNode firstCart = call(get("/me/cart"), null, 200);
        String categoryId = createCategory();
        String productId = createProduct(categoryId);
        call(post("/me/cart/items"), "{\"productId\":" + productId + "}", 200);
        call(post("/auth/logout"), null, 204);

        startSession();
        JsonNode second = register(uniqueEmail());
        JsonNode secondCart = call(get("/me/cart"), null, 200);

        assertThat(second.get("id").asLong()).isNotEqualTo(first.get("id").asLong());
        assertThat(secondCart.get("id").asText()).isNotEqualTo(firstCart.get("id").asText());
        assertThat(secondCart.get("userId").asLong()).isEqualTo(second.get("id").asLong());
        assertThat(secondCart.get("items").size()).isZero();
    }

    @Test
    void sessionCustomerIdComesFromTheSessionAttribute() throws Exception {
        startSession();
        JsonNode customer = register(uniqueEmail());

        assertThat(session.getAttribute(AuthService.SESSION_CUSTOMER_ID))
                .isEqualTo(customer.get("id").asLong());
    }
}
