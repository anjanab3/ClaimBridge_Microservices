package com.cts.claimbridge.service;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

import org.springframework.stereotype.Service;

import com.cts.claimbridge.entity.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private final static String JWT_SECRET = "bcf55f70ca05f6a311ec31e304e96e42f511d43836c83da0548fa1b2cb67ec8f";

    public Key getSigningKey()
    {
        byte[] keyBytes = JWT_SECRET.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(User user)
    {
        return Jwts.builder()
                .claim("role" , user.getRole().name())
                .claim("userId" , user.getUserId())
                .setSubject(user.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 10 * 7))
                .signWith(SignatureAlgorithm.HS256 , getSigningKey())
                .compact();
    }

    public Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String extractUserName(String token)
    {
        return extractClaims(token).getSubject();
    }

    public String extractRole(String token)
    {
        return extractClaims(token).get("role" , String.class);
    }

    public int extractUserId(String token)
    {
        return extractClaims(token).get("userId" , Integer.class);
    }

    public JwtService()
    {

    }


}
