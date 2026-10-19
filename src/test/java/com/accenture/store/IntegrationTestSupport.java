//member 3
package com.accenture.store;

import com.accenture.store.services.CsrfTokenService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Shared scaffolding for the Spring integration tests: a MockMvc request helper
 * that starts an anonymous session, carries the CSRF token and follows token
 * rotation, plus small builders for the customer, category and product fixtures.
 *
 * Each member's test class extends this and keeps only the assertions for its own
 * slice, so the HTTP setup lives in one place. Members 1-3 own the tests; the
 * former single StoreIntegrationTests class was split per member for Phase 3.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
abstract class IntegrationTestSupport {
    protected static final String CART = "/me/cart";
    protected static final String PASSWORD = "StorePass123!";

    @Autowired protected MockMvc mvc;
    @Autowired protected ObjectMapper json;

    protected MockHttpSession session;
    protected String csrfToken;

    /** Starts an anonymous session and keeps the CSRF token the interceptors expect. */
    protected void startSession() throws Exception {
        MvcResult result = mvc.perform(get("/auth/csrf"))
                .andExpect(status().isOk())
                .andReturn();

        session = (MockHttpSession) result.getRequest().getSession(false);
        csrfToken = json.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }

    /** Performs a request with the current session/CSRF token and expects the given status. */
    protected JsonNode call(MockHttpServletRequestBuilder request, String body, int status) throws Exception {
        if (session != null) request.session(session);
        if (csrfToken != null) request.header(CsrfTokenService.HEADER_NAME, csrfToken);
        if (body != null) request.contentType(MediaType.APPLICATION_JSON).content(body);

        MvcResult result = mvc.perform(request)
                .andExpect(status().is(status))
                .andReturn();

        if (session != null && !session.isInvalid()) {
            Object rotatedToken = session.getAttribute(CsrfTokenService.SESSION_ATTRIBUTE);
            if (rotatedToken instanceof String token) csrfToken = token;
        }

        String content = result.getResponse().getContentAsString();
        return content.isBlank() ? json.createObjectNode() : json.readTree(content);
    }

    /** Registers a customer through the public API; registration also creates their empty cart. */
    protected String customer() throws Exception {
        String body = json.createObjectNode()
                .put("name", "Customer")
                .put("email", "customer-" + UUID.randomUUID() + "@example.com")
                .put("password", PASSWORD)
                .toString();
        return call(post("/auth/register"), body, 201).get("id").asText();
    }

    protected String category() throws Exception {
        return call(post("/categories"), "{\"name\":\"Books\"}", 201).get("id").asText();
    }

    protected String product(String categoryId) throws Exception {
        return call(post("/products"), "{\"name\":\"Notebook\",\"description\":\"Paper\",\"price\":250,\"categoryId\":" + categoryId + "}", 201).get("id").asText();
    }
}
