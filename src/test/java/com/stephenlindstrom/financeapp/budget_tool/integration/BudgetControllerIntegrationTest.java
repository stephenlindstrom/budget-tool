package com.stephenlindstrom.financeapp.budget_tool.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.YearMonth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stephenlindstrom.financeapp.budget_tool.dto.BudgetCreateDTO;
import com.stephenlindstrom.financeapp.budget_tool.enums.TransactionType;
import com.stephenlindstrom.financeapp.budget_tool.model.Category;
import com.stephenlindstrom.financeapp.budget_tool.repository.BudgetRepository;
import com.stephenlindstrom.financeapp.budget_tool.repository.CategoryRepository;

@SpringBootTest
@AutoConfigureMockMvc
public class BudgetControllerIntegrationTest {
  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private BudgetRepository budgetRepository;

  @Autowired
  private CategoryRepository categoryRepository;

  @BeforeEach
  void setUp() {
    budgetRepository.deleteAll();
    categoryRepository.deleteAll();
  }

  @Test
  void shouldCreateBudgetSuccessfully() throws Exception {
    Category category = categoryRepository.save(Category.builder()
                        .name("Groceries")
                        .type(TransactionType.EXPENSE)
                        .build()
    ); 

    BudgetCreateDTO dto = BudgetCreateDTO.builder()
                            .value(BigDecimal.valueOf(100.00))
                            .month(YearMonth.of(2025, 6))
                            .categoryId(category.getId())
                            .build();

    mockMvc.perform(post("/api/budgets")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.value").value(100.00))
            .andExpect(jsonPath("$.month").value("2025-06"))
            .andExpect(jsonPath("$.category.name").value("Groceries"))
            .andExpect(jsonPath("$.category.type").value("EXPENSE"));
  }


}
