//member 3
package com.accenture.store.services;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Synchronizer-token CSRF protection. The token lives in the HttpSession and
 * is mirrored in a non-HttpOnly cookie so the React client can send it back
 * in the X-XSRF-TOKEN header. The session cookie itself stays HttpOnly.
 */
@Service
public class CsrfTokenService {
    public static final String COOKIE_NAME = "XSRF-TOKEN";
    public static final String HEADER_NAME = "X-XSRF-TOKEN";
    public static final String SESSION_ATTRIBUTE = "csrfToken";
    private static final int TOKEN_BYTES = 32;
    private static final SecureRandom RANDOM = new SecureRandom();

    public String getOrCreateToken(HttpSession session, HttpServletRequest request,
                                   HttpServletResponse response) {
        Object existing = session.getAttribute(SESSION_ATTRIBUTE);
        if (existing instanceof String token && !token.isBlank()) {
            return token;
        }
        return rotateToken(session, request, response);
    }

    public String rotateToken(HttpSession session, HttpServletRequest request,
                              HttpServletResponse response) {
        byte[] bytes = new byte[TOKEN_BYTES];
        RANDOM.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        session.setAttribute(SESSION_ATTRIBUTE, token);
        writeCookie(response, request, token, null);
        return token;
    }

    public boolean isValid(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return false;
        }

        Object expected = session.getAttribute(SESSION_ATTRIBUTE);
        String provided = request.getHeader(HEADER_NAME);
        if (!(expected instanceof String expectedToken) || provided == null) {
            return false;
        }

        return MessageDigest.isEqual(
                expectedToken.getBytes(StandardCharsets.UTF_8),
                provided.getBytes(StandardCharsets.UTF_8));
    }

    public void clearCookie(HttpServletRequest request, HttpServletResponse response) {
        writeCookie(response, request, "", 0L);
    }

    private void writeCookie(HttpServletResponse response, HttpServletRequest request,
                             String value, Long maxAgeSeconds) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(COOKIE_NAME, value)
                .httpOnly(false)
                .secure(request.isSecure())
                .sameSite("Lax")
                .path("/");

        if (maxAgeSeconds != null) {
            builder.maxAge(maxAgeSeconds);
        }

        response.addHeader(HttpHeaders.SET_COOKIE, builder.build().toString());
    }
}
