package org.example.digitallogisticssupplychainplatform.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.digitallogisticssupplychainplatform.service.AuthService;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
@Order(1)
@RequiredArgsConstructor
public class SimpleAuthFilter implements Filter {

    private final AuthService authService;

    private final List<String> publicEndpoints = Arrays.asList(
            "/api/auth/login",
            "/api/auth/registre",
            "/api/auth/validate-token"

    );

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String path = httpRequest.getRequestURI();
        if (isPublicEndpoint(path)) {
            chain.doFilter(request, response);
            return;
        }

        String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            sendError(response, "Token manquant. Utilisez: Authorization: Bearer VOTRE_TOKEN");
            return;
        }

        String token = authHeader.substring(7);
        if (!authService.validateToken(token)) {
            sendError(response, "Token invalide ou expiré");
            return;
        }
        chain.doFilter(request, response);
    }

    private boolean isPublicEndpoint(String path) {
        return publicEndpoints.stream().anyMatch(path::startsWith);
    }

    private void sendError(ServletResponse response, String message) throws IOException {
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        httpResponse.setContentType("application/json");
        String jsonResponse = String.format("{\"error\": \"Unauthorized\", \"message\": \"%s\"}", message);
        httpResponse.getWriter().write(jsonResponse);
    }
}