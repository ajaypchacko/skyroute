package com.skyroute.skyroute.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * A "filter" runs BEFORE a request reaches a controller.
 * This one guards the admin actions: sending flight events and the reset endpoint.
 * The caller must send the header   X-API-Key: <the configured key>
 * Everything else (reading flights, signing up, reading your own notifications) stays public.
 *
 * Note: a real production system would use Spring Security with proper authentication.
 * A shared key is a simple, honest protection for a demo.
 */
@Component
public class AdminApiKeyFilter extends OncePerRequestFilter {

    private static final String HEADER = "X-API-Key";

    private final byte[] expectedKey;

    public AdminApiKeyFilter(@Value("${skyroute.admin.api-key:}") String apiKey) {
        this.expectedKey = apiKey.getBytes(StandardCharsets.UTF_8);
    }

    // Skip the check for every path that is not an admin path.
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        boolean adminPath = path.equals("/api/flight-events") || path.startsWith("/api/admin/");
        return !adminPath;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        if (isValid(request.getHeader(HEADER))) {
            chain.doFilter(request, response);   // key is fine: carry on to the controller
            return;
        }

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/problem+json");
        response.getWriter().write(
                "{\"title\":\"Unauthorized\",\"status\":401,\"detail\":\"Missing or invalid X-API-Key header\"}");
    }

    private boolean isValid(String provided) {
        if (expectedKey.length == 0 || provided == null) {
            return false;   // no key configured, or none sent: always refuse
        }
        // isEqual takes the same time however many characters match, so the response
        // time cannot be used to guess the key one character at a time.
        return MessageDigest.isEqual(expectedKey, provided.getBytes(StandardCharsets.UTF_8));
    }
}
