package com.stephenlindstrom.financeapp.budget_tool.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import com.stephenlindstrom.financeapp.budget_tool.service.JwtService;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

public class AuthIntegrationTest extends AbstractIntegrationTest {

  @Autowired
  private JwtService jwtService;

  @Test
  void shouldRegisterUserSuccessfully() throws Exception {
    String username = "newuser_" + UUID.randomUUID();
    String payload = String.format("""
        {
          "username": "%s",
          "password": "testPassword"
        }
        """, username);

    mockMvc.perform(post("/api/auth/register")
              .contentType(MediaType.APPLICATION_JSON)
              .content(payload))
              .andExpect(status().isOk())
              .andExpect(content().string("User registered successfully")); 
  }

  @Test
  void shouldReturn400WhenRegisteringDuplicateUsername() throws Exception {
    String payload = String.format("""
      {
        "username": "%s",
        "password": "%s"
      }
      """, testUsername, testPassword);

      mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isBadRequest());
  }

  @Test
  void shouldReturn400WhenRegisteringWithMissingUsername() throws Exception {
    String payload = String.format("""
        {
          "password": "%s"
        }
        """, testPassword);

    mockMvc.perform(post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(payload))
            .andExpect(status().isBadRequest());
  }

  @Test
  void shouldReturn400WhenRegisteringWithMissingPassword() throws Exception {
    String payload = String.format("""
        {
          "username": "%s"
        }
        """, testUsername);

    mockMvc.perform(post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(payload))
            .andExpect(status().isBadRequest());
  }

  @Test
  void shouldReturnValidJwtTokenAfterLogin() throws Exception {
    String payload = buildUserJson(testUsername, testPassword);

    MvcResult result = mockMvc.perform(post("/api/auth/login")
              .contentType(MediaType.APPLICATION_JSON)
              .content(payload))
              .andExpect(status().isOk())
              .andReturn();
    
    String token = result.getResponse().getContentAsString();
    assertNotNull(token);
    assertFalse(token.isBlank());

    String extractedUsername = jwtService.extractUsername(token);
    assertEquals(testUsername, extractedUsername);
  }

  @Test
  void shouldReturn401WhenLoggingInWithInvalidCredentials() throws Exception {
    String payload = buildUserJson("InvalidUser", "InvalidPassword");

    mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(payload))
            .andExpect(status().isUnauthorized());
  }

  @Test
  void shouldReturn401WhenLoggingInWithWrongPassword() throws Exception {
    String payload = buildUserJson(testUsername, "InvalidPassword");

    mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(payload))
            .andExpect(status().isUnauthorized());
  }

  @Test
  void shouldAccessProtectedEndpointWithValidToken() throws Exception {
    String payload = buildUserJson(testUsername, testPassword);

    MvcResult result = mockMvc.perform(post("/api/auth/login")
              .contentType(MediaType.APPLICATION_JSON)
              .content(payload))
              .andExpect(status().isOk())
              .andReturn();
    
    String token = result.getResponse().getContentAsString();

    mockMvc.perform(get("/api/transactions")
            .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk());
  }

  @Test
  void shouldReturn401WhenAccessingProtectedEndpointWithoutToken() throws Exception {
    mockMvc.perform(get("/api/transactions"))
            .andExpect(status().isUnauthorized());
  }

  @Test
  void shouldReturn401WhenAccessingProtectedEndpointWithMalformedToken() throws Exception {
    mockMvc.perform(get("/api/transactions")
            .header("Authorization", "Bearer bad.token.value"))
            .andExpect(status().isUnauthorized());
  }

  @Test
  void shouldReturn401WhenAccessingProtectedEndpointWithExpiredToken() throws Exception {
    String expiredToken = generateExpiredToken("testuser");

    mockMvc.perform(get("/api/transactions")
            .header("Authorization", "Bearer " + expiredToken))
            .andExpect(status().isUnauthorized());
  }

  private String buildUserJson(String username, String password) {
    return String.format("""
        {
          "username": "%s",
          "password": "%s"
        }
        """, username, password);
  }

  private String generateExpiredToken(String username) {
    Date now = new Date();
    Date issuedAt = new Date(now.getTime() - 2 * 60 * 1000);
    Date expiredAt = new Date(now.getTime() - 60 * 1000);

    String secret = "test-secret-key-test-secret-key-test-secret-key";

    Key key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

    return Jwts.builder()
            .setSubject(username)
            .setIssuedAt(issuedAt)
            .setExpiration(expiredAt)
            .signWith(key, SignatureAlgorithm.HS256)
            .compact();
  }
}
