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
import org.springframework.http.MediaType;

import com.stephenlindstrom.financeapp.budget_tool.dto.BudgetCreateDTO;
import com.stephenlindstrom.financeapp.budget_tool.enums.TransactionType;
import com.stephenlindstrom.financeapp.budget_tool.model.Budget;
import com.stephenlindstrom.financeapp.budget_tool.model.Category;
import com.stephenlindstrom.financeapp.budget_tool.model.User;
import com.stephenlindstrom.financeapp.budget_tool.repository.BudgetRepository;
import com.stephenlindstrom.financeapp.budget_tool.repository.CategoryRepository;

public class BudgetControllerIntegrationTest extends AbstractIntegrationTest {

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
                        .user(testUser)
                        .build()
    ); 

    BudgetCreateDTO dto = BudgetCreateDTO.builder()
                            .value(BigDecimal.valueOf(100.00))
                            .month(YearMonth.of(2025, 6))
                            .categoryId(category.getId())
                            .build();

    mockMvc.perform(post("/api/budgets")
              .with(bearerToken())
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.value").value(100.00))
            .andExpect(jsonPath("$.month").value("2025-06"))
            .andExpect(jsonPath("$.category.name").value("Groceries"))
            .andExpect(jsonPath("$.category.type").value("EXPENSE"));
  }

  @Test
  void shouldReturn400WhenCreatingBudgetWithInvalidInput() throws Exception {
    BudgetCreateDTO dto = BudgetCreateDTO.builder()
                            .value(null)
                            .month(null)
                            .categoryId(null)
                            .build();

    mockMvc.perform(post("/api/budgets")
              .with(bearerToken())
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isBadRequest());
  }

  @Test
  void shouldReturnAllBudgets() throws Exception {
    Category category = categoryRepository.save(Category.builder()
                        .name("Groceries")
                        .type(TransactionType.EXPENSE)
                        .user(testUser)
                        .build()
    );

    Budget budget1 = Budget.builder()
                      .value(BigDecimal.valueOf(500.00))
                      .month(YearMonth.of(2025, 6))
                      .category(category)
                      .user(testUser)
                      .build();
    
    Budget budget2 = Budget.builder()
                      .value(BigDecimal.valueOf(400.00))
                      .month(YearMonth.of(2025, 5))
                      .category(category)
                      .user(testUser)
                      .build();

    budgetRepository.saveAll(List.of(budget1, budget2));
    
    mockMvc.perform(get("/api/budgets")
            .with(bearerToken()))
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
  void shouldOnlyReturnCurrentUserBudgets() throws Exception {
    User anotherUser = userRepository.save(
      User.builder().username("anotherUser").password("hashedPassword").build()
    );                 

    Category category1 = categoryRepository.save(Category.builder()
                        .name("Groceries")
                        .type(TransactionType.EXPENSE)
                        .user(testUser)
                        .build()
    );

    Category category2 = categoryRepository.save(Category.builder()
                        .name("Groceries")
                        .type(TransactionType.EXPENSE)
                        .user(anotherUser)
                        .build()
    );

    Budget budget1 = Budget.builder()
                      .value(BigDecimal.valueOf(500.00))
                      .month(YearMonth.of(2025, 6))
                      .category(category1)
                      .user(testUser)
                      .build();
    
    Budget budget2 = Budget.builder()
                      .value(BigDecimal.valueOf(400.00))
                      .month(YearMonth.of(2025, 5))
                      .category(category1)
                      .user(testUser)
                      .build();

    Budget budget3 = Budget.builder()
                      .value(BigDecimal.valueOf(600.00))
                      .month(YearMonth.of(2025, 7))
                      .category(category2)
                      .user(anotherUser)
                      .build();

    budgetRepository.saveAll(List.of(budget1, budget2, budget3));

    mockMvc.perform(get("/api/budgets")
            .with(bearerToken()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].value").value(500.00))
            .andExpect(jsonPath("$[1].value").value(400.00))
            .andExpect(jsonPath("$[?(@.value == 600.00)]").doesNotExist());
  }

  @Test
  void shouldReturnBudgetById() throws Exception {
    Category category = categoryRepository.save(Category.builder()
                        .name("Groceries")
                        .type(TransactionType.EXPENSE)
                        .user(testUser)
                        .build()
    );

    Budget budget = budgetRepository.save(Budget.builder()
                      .value(BigDecimal.valueOf(500.00))
                      .month(YearMonth.of(2025, 6))
                      .category(category)
                      .user(testUser)
                      .build()
    );

    mockMvc.perform(get("/api/budgets/{id}", budget.getId())
            .with(bearerToken()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.value").value(500.00))
            .andExpect(jsonPath("$.month").value("2025-06"))
            .andExpect(jsonPath("$.category.name").value("Groceries"))
            .andExpect(jsonPath("$.category.type").value("EXPENSE"));
  }

  @Test
  void shouldReturn404WhenGettingNonExistentBudget() throws Exception {
    mockMvc.perform(get("/api/budgets/{id}", 999L)
            .with(bearerToken()))
            .andExpect(status().isNotFound());
  }

  @Test
  void shouldReturn404WhenAccessingAnotherUsersBudgetById() throws Exception {
    User anotherUser = userRepository.save(
      User.builder().username("anotherUser").password("hashedPassword").build()
    );

    Category category = categoryRepository.save(Category.builder()
                        .name("Groceries")
                        .type(TransactionType.EXPENSE)
                        .user(anotherUser)
                        .build()
    );

    Budget budget = budgetRepository.save(Budget.builder()
                      .value(BigDecimal.valueOf(500.00))
                      .month(YearMonth.of(2025, 6))
                      .category(category)
                      .user(anotherUser)
                      .build()
    );

    mockMvc.perform(get("/api/budgets/{id}", budget.getId())
            .with(bearerToken()))
            .andExpect(status().isNotFound());
  }

  @Test
  void shouldReturnAvailableMonths() throws Exception {
    Category category = categoryRepository.save(Category.builder()
                        .name("Groceries")
                        .type(TransactionType.EXPENSE)
                        .user(testUser)
                        .build()
    );

    Budget budget1 = Budget.builder()
                      .value(BigDecimal.valueOf(500.00))
                      .month(YearMonth.of(2025, 6))
                      .category(category)
                      .user(testUser)
                      .build();
    
    Budget budget2 = Budget.builder()
                      .value(BigDecimal.valueOf(400.00))
                      .month(YearMonth.of(2025, 5))
                      .category(category)
                      .user(testUser)
                      .build();

    budgetRepository.saveAll(List.of(budget1, budget2));

    mockMvc.perform(get("/api/budgets/months")
            .with(bearerToken()))
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
                        .user(testUser)
                        .build()
    );
    
    Budget budget = budgetRepository.save(Budget.builder()
                      .value(BigDecimal.valueOf(500.00))
                      .month(YearMonth.of(2025, 5))
                      .category(category)
                      .user(testUser)
                      .build()
    );

    BudgetCreateDTO dto = BudgetCreateDTO.builder()
                            .value(BigDecimal.valueOf(100.00))
                            .month(YearMonth.of(2025, 6))
                            .categoryId(category.getId())
                            .build();
    
    mockMvc.perform(put("/api/budgets/{id}", budget.getId())
              .with(bearerToken())
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
  void shouldReturn404WhenUpdatingNonExistentBudget() throws Exception {
    Category category = categoryRepository.save(Category.builder()
                        .name("Groceries")
                        .type(TransactionType.EXPENSE)
                        .user(testUser)
                        .build()
    );

    BudgetCreateDTO dto = BudgetCreateDTO.builder()
                            .value(BigDecimal.valueOf(100.00))
                            .month(YearMonth.of(2025, 6))
                            .categoryId(category.getId())
                            .build();

    mockMvc.perform(put("/api/budgets/{id}", 999L)
              .with(bearerToken())
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isNotFound());
  }

  @Test
  void shouldReturn404WhenUpdatingWithInvalidCategoryId() throws Exception {
    Category category = categoryRepository.save(Category.builder()
                        .name("Groceries")
                        .type(TransactionType.EXPENSE)
                        .user(testUser)
                        .build()
    );

    Budget budget = budgetRepository.save(Budget.builder()
                      .value(BigDecimal.valueOf(500.00))
                      .month(YearMonth.of(2025, 5))
                      .category(category)
                      .user(testUser)
                      .build()
    );

    BudgetCreateDTO dto = BudgetCreateDTO.builder()
                            .value(BigDecimal.valueOf(100.00))
                            .month(YearMonth.of(2025, 6))
                            .categoryId(999L)
                            .build();

    mockMvc.perform(put("/api/budgets/{id}", budget.getId())
              .with(bearerToken())
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isNotFound());
  }

  @Test
  void shouldDeleteBudgetAndReturnNoContent() throws Exception {
    Category category = categoryRepository.save(Category.builder()
                        .name("Groceries")
                        .type(TransactionType.EXPENSE)
                        .user(testUser)
                        .build()
    );

    Budget budget = budgetRepository.save(Budget.builder()
                      .value(BigDecimal.valueOf(500.00))
                      .month(YearMonth.of(2025, 6))
                      .category(category)
                      .user(testUser)
                      .build()
    );

    mockMvc.perform(delete("/api/budgets/{id}", budget.getId())
            .with(bearerToken()))
            .andExpect(status().isNoContent());

    assertFalse(budgetRepository.findByIdAndUser(budget.getId(), testUser).isPresent());
  }

  @Test
  void shouldReturnNoContentWhenDeletingNonExistentBudget() throws Exception {
    mockMvc.perform(delete("/api/budgets/{id}", 999L)
            .with(bearerToken()))
            .andExpect(status().isNoContent());
  }
}
