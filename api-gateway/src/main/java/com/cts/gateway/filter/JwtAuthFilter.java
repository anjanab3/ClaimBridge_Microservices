package com.cts.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Validates the JWT on every inbound request before routing to downstream services.
 * Public paths (login, public registration, internal service-to-service calls) bypass validation.
 * On success, X-User-Id / X-User-Role / X-Username headers are injected for downstream use.
 */
@Slf4j
@Component
public class JwtAuthFilter implements GlobalFilter, Ordered {

    // Paths that do not require a valid JWT
    private static final List<String> PUBLIC_PATHS = List.of(
        "/api/identity/auth/login",
        "/api/identity/auth/register/public",
        "/api/internal/",
        "/actuator",
        "/swagger-ui",
        "/v3/api-docs"
    );

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        if (isPublic(path)) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Missing or invalid Authorization header: {}", path);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);
        try {
            byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(keyBytes))
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String userId   = String.valueOf(claims.get("userId"));
            String role     = claims.get("role", String.class);
            String username = claims.getSubject();

            log.debug("Authenticated: user={} role={} path={}", username, role, path);

            ServerWebExchange mutated = exchange.mutate()
                    .request(r -> r
                            .header("X-User-Id",   userId   != null ? userId   : "")
                            .header("X-User-Role", role     != null ? role     : "")
                            .header("X-Username",  username != null ? username : ""))
                    .build();

            return chain.filter(mutated);

        } catch (JwtException e) {
            log.warn("JWT validation failed [{}]: {}", path, e.getMessage());
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }

    private boolean isPublic(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
