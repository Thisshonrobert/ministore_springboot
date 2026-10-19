//member 4
package com.accenture.store.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final AuthInterceptor authInterceptor;
    private final CsrfInterceptor csrfInterceptor;

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    public WebConfig(AuthInterceptor authInterceptor, CsrfInterceptor csrfInterceptor) {
        this.authInterceptor = authInterceptor;
        this.csrfInterceptor = csrfInterceptor;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(allowedOrigins.split("\\s*,\\s*"))
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("Content-Type", "Accept", "X-XSRF-TOKEN", "X-Requested-With")
                .exposedHeaders("Location", "X-CSRF-TOKEN")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Mutation endpoints that use the session cookie need a CSRF token. The
        // chat and the summary endpoints are read-only for the store, but they
        // still start work on the server, so they are protected the same way.
        registry.addInterceptor(csrfInterceptor)
                .addPathPatterns("/auth/register", "/auth/login", "/auth/logout", "/me/**",
                        "/chat", "/products/*/reviews/summary");

        // /me/** derives the customer from the session; no customer ID is trusted from the client.
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/me/**");
    }
}
