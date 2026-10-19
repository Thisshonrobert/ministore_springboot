//member 3
package com.accenture.store.controllers;

import com.accenture.store.dtos.CsrfTokenResponse;
import com.accenture.store.dtos.LoginRequest;
import com.accenture.store.dtos.RegisterRequest;
import com.accenture.store.dtos.UserDto;
import com.accenture.store.exceptions.UnauthorizedException;
import com.accenture.store.services.AuthService;
import com.accenture.store.services.CsrfTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;
    private final CsrfTokenService csrfTokenService;

    public AuthController(AuthService authService, CsrfTokenService csrfTokenService) {
        this.authService = authService;
        this.csrfTokenService = csrfTokenService;
    }

    /**
     * Starts an anonymous session and returns the token the client must echo in
     * the X-XSRF-TOKEN header for register, login and logout.
     */
    @GetMapping("/csrf")
    public CsrfTokenResponse csrf(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(true);
        String token = csrfTokenService.getOrCreateToken(session, request, response);
        return new CsrfTokenResponse(token);
    }

    @PostMapping("/register")
    public ResponseEntity<UserDto> register(@Valid @RequestBody RegisterRequest body,
                                            HttpServletRequest request,
                                            HttpServletResponse response) {
        UserDto customer = authService.register(body);
        establishSession(request, response, customer.getId());
        return ResponseEntity.created(URI.create("/auth/me")).body(customer);
    }

    @PostMapping("/login")
    public UserDto login(@Valid @RequestBody LoginRequest body,
                         HttpServletRequest request,
                         HttpServletResponse response) {
        UserDto customer = authService.authenticate(body);
        establishSession(request, response, customer.getId());
        return customer;
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        csrfTokenService.clearCookie(request, response);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public UserDto me(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Object customerId = session == null ? null : session.getAttribute(AuthService.SESSION_CUSTOMER_ID);

        if (!(customerId instanceof Long id)) {
            throw new UnauthorizedException("You are not signed in.");
        }

        return authService.getCustomer(id);
    }

    private void establishSession(HttpServletRequest request, HttpServletResponse response, Long customerId) {
        HttpSession session = request.getSession(true);

        // Session fixation protection: keep the session data but issue a new session ID.
        request.changeSessionId();
        session.setAttribute(AuthService.SESSION_CUSTOMER_ID, customerId);

        // The CSRF token is rotated with the authenticated session and returned in a
        // header as well as the cookie, which also works for a cross-origin client.
        String csrfToken = csrfTokenService.rotateToken(session, request, response);
        response.setHeader("X-CSRF-TOKEN", csrfToken);
    }
}
