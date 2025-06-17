package com.stephenlindstrom.financeapp.budget_tool.integration;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stephenlindstrom.financeapp.budget_tool.repository.CategoryRepository;

@SpringBootTest
@AutoConfigureMockMvc
public class CategoryControllerIntegrationTest {
  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private CategoryRepository categoryRepository;

  @BeforeEach
  void setUp() {
    categoryRepository.deleteAll();
  }
}
