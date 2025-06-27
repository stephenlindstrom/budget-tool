package com.stephenlindstrom.financeapp.budget_tool.integration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stephenlindstrom.financeapp.budget_tool.dto.BudgetCreateDTO;
import com.stephenlindstrom.financeapp.budget_tool.enums.TransactionType;
import com.stephenlindstrom.financeapp.budget_tool.model.Budget;
import com.stephenlindstrom.financeapp.budget_tool.model.Category;
import com.stephenlindstrom.financeapp.budget_tool.repository.BudgetRepository;
import com.stephenlindstrom.financeapp.budget_tool.repository.CategoryRepository;

@ActiveProfiles("test")
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

  @Test
  void shouldReturnAllBudgets() throws Exception {
    Category category = categoryRepository.save(Category.builder()
                        .name("Groceries")
                        .type(TransactionType.EXPENSE)
                        .build()
    );

    Budget budget1 = Budget.builder()
                      .value(BigDecimal.valueOf(500.00))
                      .month(YearMonth.of(2025, 6))
                      .category(category)
                      .build();
    
    Budget budget2 = Budget.builder()
                      .value(BigDecimal.valueOf(400.00))
                      .month(YearMonth.of(2025, 5))
                      .category(category)
                      .build();

    budgetRepository.saveAll(List.of(budget1, budget2));
    
    mockMvc.perform(get("/api/budgets"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].value").value(500.00))
            .andExpect(jsonPath("$[0].month").value("2025-06"))
            .andExpect(jsonPath("$[0].category.name").value("Groceries"))
            .andExpect(jsonPath("$[0].category.type").value("EXPENSE"))
            .andExpect(jsonPath("$[1].value").value(400.00))
            .andExpect(jsonPath("$[1].month").value("2025-05"))
            .andExpect(jsonPath("$[1].category.name").value("Groceries"))
            .andExpect(jsonPath("$[1].category.type").value("EXPENSE"));
  }

  @Test
  void shouldReturnBudgetById() throws Exception {
    Category category = categoryRepository.save(Category.builder()
                        .name("Groceries")
                        .type(TransactionType.EXPENSE)
                        .build()
    );

    Budget budget = budgetRepository.save(Budget.builder()
                      .value(BigDecimal.valueOf(500.00))
                      .month(YearMonth.of(2025, 6))
                      .category(category)
                      .build()
    );

    mockMvc.perform(get("/api/budgets/{id}", budget.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.value").value(500.00))
            .andExpect(jsonPath("$.month").value("2025-06"))
            .andExpect(jsonPath("$.category.name").value("Groceries"))
            .andExpect(jsonPath("$.category.type").value("EXPENSE"));
  }

  @Test
  void shouldReturn404WhenGettingNonExistentBudget() throws Exception {
    mockMvc.perform(get("/api/budgets/{id}", 999L))
            .andExpect(status().isNotFound());
  }

  @Test
  void shouldReturnAvailableMonths() throws Exception {
    Category category = categoryRepository.save(Category.builder()
                        .name("Groceries")
                        .type(TransactionType.EXPENSE)
                        .build()
    );

    Budget budget1 = Budget.builder()
                      .value(BigDecimal.valueOf(500.00))
                      .month(YearMonth.of(2025, 6))
                      .category(category)
                      .build();
    
    Budget budget2 = Budget.builder()
                      .value(BigDecimal.valueOf(400.00))
                      .month(YearMonth.of(2025, 5))
                      .category(category)
                      .build();

    budgetRepository.saveAll(List.of(budget1, budget2));

    mockMvc.perform(get("/api/budgets/months"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].value").value("2025-06"))
            .andExpect(jsonPath("$[0].display").value("June 2025"))
            .andExpect(jsonPath("$[1].value").value("2025-05"))
            .andExpect(jsonPath("$[1].display").value("May 2025"));
  }

  @Test
  void shouldUpdateBudgetById() throws Exception {
    Category category = categoryRepository.save(Category.builder()
                        .name("Groceries")
                        .type(TransactionType.EXPENSE)
                        .build()
    );
    
    Budget budget = budgetRepository.save(Budget.builder()
                      .value(BigDecimal.valueOf(500.00))
                      .month(YearMonth.of(2025, 5))
                      .category(category)
                      .build()
    );

    BudgetCreateDTO dto = BudgetCreateDTO.builder()
                            .value(BigDecimal.valueOf(100.00))
                            .month(YearMonth.of(2025, 6))
                            .categoryId(category.getId())
                            .build();
    
    mockMvc.perform(put("/api/budgets/{id}", budget.getId())
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(budget.getId()))
            .andExpect(jsonPath("$.value").value(100.00))
            .andExpect(jsonPath("$.month").value("2025-06"))
            .andExpect(jsonPath("$.category.name").value("Groceries"))
            .andExpect(jsonPath("$.category.type").value("EXPENSE"));
  }

  @Test
  void shouldDeleteBudgetAndReturnNoContent() throws Exception {
    Category category = categoryRepository.save(Category.builder()
                        .name("Groceries")
                        .type(TransactionType.EXPENSE)
                        .build()
    );

    Budget budget = budgetRepository.save(Budget.builder()
                      .value(BigDecimal.valueOf(500.00))
                      .month(YearMonth.of(2025, 6))
                      .category(category)
                      .build()
    );

    mockMvc.perform(delete("/api/budgets/{id}", budget.getId()))
            .andExpect(status().isNoContent());

    assertFalse(budgetRepository.findById(budget.getId()).isPresent());
  }
}
