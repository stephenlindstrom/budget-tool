package com.stephenlindstrom.financeapp.budget_tool.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.transaction.Transactional;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public abstract class AbstractIntegrationTest {

  @Autowired
  protected MockMvc mockMvc;

  @Autowired
  protected ObjectMapper objectMapper;

  protected String testUsername;
  protected String testPassword;
  protected String jwtToken;

  @BeforeEach
  void authenticateTestUser() throws Exception {
    testUsername = "user_" + UUID.randomUUID();
    testPassword = "testPassword";

    registerTestUser(testUsername, testPassword);
    jwtToken = loginAndGetToken(testUsername, testPassword);
  }

  private void registerTestUser(String username, String password) throws Exception {
    String registerPayload = String.format("""
        {
          "username": "%s",
          "password": "%s"
        }
        """, username, password);

    mockMvc.perform(post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(registerPayload))
            .andExpect(status().isOk());
  }

  private String loginAndGetToken(String username, String password) throws Exception {
    String loginPayload = String.format("""
        {
          "username": "%s",
          "password": "%s"
        }
        """, username, password);

    MvcResult result = mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(loginPayload))
            .andExpect(status().isOk())
            .andReturn();
    
    return result.getResponse().getContentAsString();
  }

  protected RequestPostProcessor bearerToken() {
    return request -> {
      request.addHeader("Authorization", "Bearer " + jwtToken);
      return request;
    };
  }
}
