//member 3
package com.accenture.store.config;

import com.accenture.store.services.CsrfTokenService;
import com.accenture.store.exceptions.ForbiddenException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class CsrfInterceptor implements HandlerInterceptor {
    private final CsrfTokenService csrfTokenService;

    public CsrfInterceptor(CsrfTokenService csrfTokenService) {
        this.csrfTokenService = csrfTokenService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String method = request.getMethod();
        if ("GET".equals(method) || "HEAD".equals(method) || "OPTIONS".equals(method)) {
            return true;
        }

        if (!csrfTokenService.isValid(request)) {
            throw new ForbiddenException("Your security token is missing or stale. Refresh the page and try again.");
        }
        return true;
    }
}
