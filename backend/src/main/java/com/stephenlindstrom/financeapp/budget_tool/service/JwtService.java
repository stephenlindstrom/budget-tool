package com.stephenlindstrom.financeapp.budget_tool.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Base64;
import java.util.Date;

@Service
public class JwtService {

    // Injects the secret key from application properties
    @Value("${jwt.secret}")
    private String secretKey;

    // 1 day in milliseconds
    private static final long EXPIRATION_TIME = 86400000;

    /**
     * Generates the cryptographic key used to sign and validate JWT tokens.
     */
    private Key getSigningKey() {
      byte[] keyBytes = Base64.getDecoder().decode(secretKey);
      return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Generates a JWT token for a given username.
     */
    public String generateToken(String username) {
      return Jwts.builder()
            .setSubject(username)
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
            .signWith(getSigningKey(), SignatureAlgorithm.HS256)
            .compact();
    }

    /**
     * Extracts the username from a given JWT token.
     */
    public String extractUsername(String token) {
      return Jwts.parserBuilder()
            .setSigningKey(getSigningKey())
            .build()
            .parseClaimsJws(token)
            .getBody()
            .getSubject();
    }

    /**
     * Validates the token against the provided username and checks that it hasn't expired.
     */
    public boolean isTokenValid(String token, String username) {
      return username.equals(extractUsername(token)) && !isTokenExpired(token);
    }

    /**
     * Checks whether the token has expired.
     */
    private boolean isTokenExpired(String token) {
      return Jwts.parserBuilder()
            .setSigningKey(getSigningKey())
            .build()
            .parseClaimsJws(token)
            .getBody()
            .getExpiration()
            .before(new Date());
    }
}
