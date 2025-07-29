package com.stephenlindstrom.financeapp.budget_tool.service;

import static org.junit.jupiter.api.Assertions.*;

import java.security.Key;
import java.util.Base64;
import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

public class JwtServiceTest {

  private JwtService jwtService;
  private final String secret = Base64.getEncoder().encodeToString("my-test-secret-key-12345678901234567890".getBytes());

  @BeforeEach
  void setup() {
    jwtService = new JwtService();
    ReflectionTestUtils.setField(jwtService, "secretKey", secret);
  }

  @Test
  void testGenerateTokenAndExtractUsername() {
    String username = "testuser";
    String token = jwtService.generateToken(username);

    assertNotNull(token);
    String extractedUsername = jwtService.extractUsername(token);
    assertEquals(username, extractedUsername);
  }

  @Test
  void testIsTokenValid() {
    String username = "testuser";
    String token = jwtService.generateToken(username);

    assertTrue(jwtService.isTokenValid(token, username));
    assertFalse(jwtService.isTokenValid(token, "InvalidUser"));
  }

  @Test
  void testExpiredTokenIsInvalid() {
    String username = "expiredUser";
    Key key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secret));

    String expiredToken = Jwts.builder()
                          .setSubject(username)
                          .setIssuedAt(new Date(System.currentTimeMillis() - 2000))
                          .setExpiration(new Date(System.currentTimeMillis() - 1000))
                          .signWith(key, SignatureAlgorithm.HS256)
                          .compact();

    assertThrows(ExpiredJwtException.class, () -> {
      jwtService.isTokenValid(expiredToken, username);
    });
  }

  @Test
  void testMalformedTokenIsInvalid() {
    String malformedToken = "this.is.not.a.valid.token";
    String username = "testuser";

    assertThrows(JwtException.class, () -> {
      jwtService.isTokenValid(malformedToken, username);
    });
  }

  @Test
  void testNullTokenIsInvalid() {
    String username = "testuser";

    assertThrows(IllegalArgumentException.class, () -> {
      jwtService.isTokenValid(null, username);
    });
  }

  @Test
  void testEmptyTokenIsInvalid() {
    String username = "testuser";

    assertThrows(IllegalArgumentException.class, () -> {
      jwtService.isTokenValid("", username);
    });
  }

}
