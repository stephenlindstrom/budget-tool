package com.stephenlindstrom.financeapp.budget_tool.integration;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import com.stephenlindstrom.financeapp.budget_tool.dto.TransactionCreateDTO;
import com.stephenlindstrom.financeapp.budget_tool.enums.TransactionType;
import com.stephenlindstrom.financeapp.budget_tool.model.Category;
import com.stephenlindstrom.financeapp.budget_tool.model.Transaction;
import com.stephenlindstrom.financeapp.budget_tool.model.User;
import com.stephenlindstrom.financeapp.budget_tool.repository.CategoryRepository;
import com.stephenlindstrom.financeapp.budget_tool.repository.TransactionRepository;

public class TransactionControllerIntegrationTest extends AbstractIntegrationTest {

  @Autowired
  private TransactionRepository transactionRepository;

  @Autowired
  private CategoryRepository categoryRepository;

  @BeforeEach
  void setUp() {
    transactionRepository.deleteAll();
    categoryRepository.deleteAll();
  }

  @Test
  void shouldCreateTransactionSuccessfully() throws Exception {
    Category category = categoryRepository.save(
      Category.builder()
        .name("Groceries")
        .type(TransactionType.EXPENSE)
        .user(testUser)
        .build()
    );

    TransactionCreateDTO dto = TransactionCreateDTO.builder()
                                .amount(BigDecimal.valueOf(50.00))
                                .categoryId(category.getId())
                                .type(TransactionType.EXPENSE)
                                .date(LocalDate.of(2025, 6, 3))
                                .description("food")
                                .build();
    
    mockMvc.perform(post("/api/transactions")
            .with(bearerToken())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(dto)))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.amount").value(50.00))
          .andExpect(jsonPath("$.description").value("food"));

  }

  @Test
  void shouldReturn400WhenCreatingTransactionWithInvalidInput() throws Exception {
    TransactionCreateDTO dto = TransactionCreateDTO.builder()
                                .amount(null)
                                .categoryId(null)
                                .type(null)
                                .date(null)
                                .description(null)
                                .build();

    mockMvc.perform(post("/api/transactions")
              .with(bearerToken())
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isBadRequest());
  }

  @Test
  void shouldReturnAllTransactions() throws Exception {
    Category category = categoryRepository.save(
      Category.builder()
        .name("Groceries")
        .type(TransactionType.EXPENSE)
        .user(testUser)
        .build()
    );

    Transaction transaction1 = Transaction.builder()
                                .amount(BigDecimal.valueOf(100.00))
                                .category(category)
                                .type(category.getType())
                                .date(LocalDate.of(2025, 6, 12))
                                .description("Fry's")
                                .user(testUser)
                                .build();

    Transaction transaction2 = Transaction.builder()
                                .amount(BigDecimal.valueOf(50.00))
                                .category(category)
                                .type(category.getType())
                                .date(LocalDate.of(2025, 6, 4))
                                .description("Whole Foods")
                                .user(testUser)
                                .build();

    transactionRepository.saveAll(List.of(transaction1, transaction2));

    mockMvc.perform(get("/api/transactions")
          .with(bearerToken()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.length()").value(2))
          .andExpect(jsonPath("$[0].amount").value(100.00))
          .andExpect(jsonPath("$[0].category.name").value("Groceries"))
          .andExpect(jsonPath("$[0].type").value("EXPENSE"))
          .andExpect(jsonPath("$[0].date").value("2025-06-12"))
          .andExpect(jsonPath("$[0].description").value("Fry's"))
          .andExpect(jsonPath("$[1].amount").value(50.00));
  }

  @Test
  void shouldOnlyReturnCurrentUserTransactions() throws Exception {
    User anotherUser = userRepository.save(
      User.builder().username("anotherUser").password("hashedPassword").build()
    );

    Category category1 = categoryRepository.save(
      Category.builder()
        .name("Groceries")
        .type(TransactionType.EXPENSE)
        .user(testUser)
        .build()
    );

    Category category2 = categoryRepository.save(
      Category.builder()
        .name("Groceries")
        .type(TransactionType.EXPENSE)
        .user(anotherUser)
        .build()
    );

    Transaction transaction1 = Transaction.builder()
                                .amount(BigDecimal.valueOf(100.00))
                                .category(category1)
                                .type(category1.getType())
                                .date(LocalDate.of(2025, 6, 12))
                                .description("Fry's")
                                .user(testUser)
                                .build();

    Transaction transaction2 = Transaction.builder()
                                .amount(BigDecimal.valueOf(50.00))
                                .category(category1)
                                .type(category1.getType())
                                .date(LocalDate.of(2025, 6, 4))
                                .description("Whole Foods")
                                .user(testUser)
                                .build();
    
    Transaction transaction3 = Transaction.builder()
                                .amount(BigDecimal.valueOf(75.00))
                                .category(category2)
                                .type(category2.getType())
                                .date(LocalDate.of(2025, 7, 5))
                                .description("Fry's")
                                .user(anotherUser)
                                .build();

    transactionRepository.saveAll(List.of(transaction1, transaction2, transaction3));

    mockMvc.perform(get("/api/transactions")
            .with(bearerToken()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].amount").value(100.00))
            .andExpect(jsonPath("$[1].amount").value(50.00))
            .andExpect(jsonPath("$[?(@.amount == 75.00)]").doesNotExist());
  }

  @Test
  void shouldReturnFilteredTransactions() throws Exception {
    Category category = categoryRepository.save(
      Category.builder()
        .name("Groceries")
        .type(TransactionType.EXPENSE)
        .user(testUser)
        .build()
    );

    Transaction transaction1 = Transaction.builder()
                                .amount(BigDecimal.valueOf(100.00))
                                .category(category)
                                .type(category.getType())
                                .date(LocalDate.of(2025, 6, 12))
                                .description("Fry's")
                                .user(testUser)
                                .build();

    Transaction transaction2 = Transaction.builder()
                                .amount(BigDecimal.valueOf(50.00))
                                .category(category)
                                .type(category.getType())
                                .date(LocalDate.of(2025, 6, 4))
                                .description("Whole Foods")
                                .user(testUser)
                                .build();

    Transaction transaction3 = Transaction.builder()
                                .amount(BigDecimal.valueOf(25.00))
                                .category(category)
                                .type(category.getType())
                                .date(LocalDate.of(2024, 11, 23))
                                .description("Target")
                                .user(testUser)
                                .build();

    transactionRepository.saveAll(List.of(transaction1, transaction2, transaction3));

    mockMvc.perform(get("/api/transactions/filter")
            .param("startDate", "2025-06-01")
            .with(bearerToken()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.length()").value(2))
          .andExpect(jsonPath("$[0].date").value("2025-06-12"))
          .andExpect(jsonPath("$[1].date").value("2025-06-04"));
  }

  @Test 
  void shouldUpdateTransactionById() throws Exception {
    Category category1 = categoryRepository.save(
      Category.builder()
        .name("Groceries")
        .type(TransactionType.EXPENSE)
        .user(testUser)
        .build()
    );

    Category category2 = categoryRepository.save(
      Category.builder()
        .name("Salary")
        .type(TransactionType.INCOME)
        .user(testUser)
        .build()
    );



    Transaction transaction = transactionRepository.save(
      Transaction.builder()
        .amount(BigDecimal.valueOf(100.00))
        .category(category1)
        .type(TransactionType.EXPENSE)
        .date(LocalDate.of(2025, 5, 31))
        .description("Fry's")
        .user(testUser)
        .build() 
    );

    TransactionCreateDTO dto = TransactionCreateDTO.builder()
                                .amount(BigDecimal.valueOf(50.00))
                                .categoryId(category2.getId())
                                .type(TransactionType.INCOME)
                                .date(LocalDate.of(2025, 6, 3))
                                .description("Paycheck")
                                .build();
    
    mockMvc.perform(put("/api/transactions/{id}", transaction.getId())
              .with(bearerToken())
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(transaction.getId()))
            .andExpect(jsonPath("$.amount").value(50.00))
            .andExpect(jsonPath("$.category.name").value("Salary"))
            .andExpect(jsonPath("$.category.type").value("INCOME"))
            .andExpect(jsonPath("$.type").value("INCOME"))
            .andExpect(jsonPath("$.date").value("2025-06-03"))
            .andExpect(jsonPath("$.description").value("Paycheck"));   
  }

  @Test 
  void shouldReturn404WhenUpdatingNonExistentTransaction() throws Exception {
    Category category = categoryRepository.save(
      Category.builder()
        .name("Salary")
        .type(TransactionType.INCOME)
        .user(testUser)
        .build()
    );

    TransactionCreateDTO dto = TransactionCreateDTO.builder()
                                .amount(BigDecimal.valueOf(50.00))
                                .categoryId(category.getId())
                                .type(TransactionType.INCOME)
                                .date(LocalDate.of(2025, 6, 3))
                                .description("Paycheck")
                                .build();

    mockMvc.perform(put("/api/transactions/{id}", 999L)
              .with(bearerToken())
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isNotFound());
  }

  @Test
  void shouldReturn404WhenUpdatingWithInvalidCategoryId() throws Exception {
    Category category = categoryRepository.save(
      Category.builder()
        .name("Groceries")
        .type(TransactionType.EXPENSE)
        .user(testUser)
        .build()
    );

    Transaction transaction = transactionRepository.save(
      Transaction.builder()
        .amount(BigDecimal.valueOf(100.00))
        .category(category)
        .type(TransactionType.EXPENSE)
        .date(LocalDate.of(2025, 5, 31))
        .description("Fry's")
        .user(testUser)
        .build() 
    );

    TransactionCreateDTO dto = TransactionCreateDTO.builder()
                                .amount(BigDecimal.valueOf(50.00))
                                .categoryId(999L)
                                .type(TransactionType.INCOME)
                                .date(LocalDate.of(2025, 6, 3))
                                .description("Invalid category test")
                                .build();
    
    mockMvc.perform(put("/api/transactions/{id}", transaction.getId())
              .with(bearerToken())
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isNotFound());
  }

  @Test
  void shouldDeleteTransactionAndReturnNoContent() throws Exception {
    Category category = categoryRepository.save(
        Category.builder()
            .name("Groceries")
            .type(TransactionType.EXPENSE)
            .user(testUser)
            .build()
    );


    Transaction transaction1 = transactionRepository.save(
      Transaction.builder()
          .amount(BigDecimal.valueOf(100.00))
          .category(category)
          .type(category.getType())
          .date(LocalDate.of(2025, 6, 12))
          .description("Fry's")
          .user(testUser)
          .build()
    );

    mockMvc.perform(delete("/api/transactions/{id}", transaction1.getId())
          .with(bearerToken()))
          .andExpect(status().isNoContent());

    assertFalse(transactionRepository.findByIdAndUser(transaction1.getId(), testUser).isPresent());
  }

  @Test
  void shouldReturnNoContentWhenDeletingNonExistentTransaction() throws Exception {
    mockMvc.perform(delete("/api/transactions/{id}", 999L)
            .with(bearerToken()))
            .andExpect(status().isNoContent());
  }
}
