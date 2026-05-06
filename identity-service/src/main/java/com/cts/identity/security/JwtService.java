package com.cts.identity.security;

import com.cts.identity.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Service
public class JwtService {

    // Must match the secret used in claims-service so tokens are mutually valid
    private static final String JWT_SECRET = "bcf55f70ca05f6a311ec31e304e96e42f511d43836c83da0548fa1b2cb67ec8f";

    public Key getSigningKey() {
        byte[] keyBytes = JWT_SECRET.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(User user) {
        var builder = Jwts.builder()
                .claim("role",     user.getRole().name())
                .claim("userId",   user.getUserId())
                .claim("username", user.getUsername())
                .setSubject(user.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 70));

        // Include holderId for USER role so policyholder portal can filter data
        if (user.getHolderId() != null) {
            builder.claim("holderId", user.getHolderId());
        }

        return builder.signWith(SignatureAlgorithm.HS256, getSigningKey()).compact();
    }

    public Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    public String extractRole(String token) {
        return extractClaims(token).get("role", String.class);
    }

    public String extractUserId(String token) {
        return extractClaims(token).get("userId", String.class);
    }
}
