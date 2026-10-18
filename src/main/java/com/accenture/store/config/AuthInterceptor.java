//member 3
package com.codewithmosh.store.config;

import com.codewithmosh.store.exceptions.UnauthorizedException;
import com.codewithmosh.store.services.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        HttpSession session = request.getSession(false);
        if (session == null || !(session.getAttribute(AuthService.SESSION_CUSTOMER_ID) instanceof Long)) {
            throw new UnauthorizedException("Please sign in to continue.");
        }
        return true;
    }
}
