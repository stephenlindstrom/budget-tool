package com.stephenlindstrom.financeapp.budget_tool.integration;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stephenlindstrom.financeapp.budget_tool.dto.TransactionCreateDTO;
import com.stephenlindstrom.financeapp.budget_tool.enums.TransactionType;
import com.stephenlindstrom.financeapp.budget_tool.model.Category;
import com.stephenlindstrom.financeapp.budget_tool.model.Transaction;
import com.stephenlindstrom.financeapp.budget_tool.repository.CategoryRepository;
import com.stephenlindstrom.financeapp.budget_tool.repository.TransactionRepository;

@SpringBootTest
@AutoConfigureMockMvc
public class TransactionControllerIntegrationTest {
  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private TransactionRepository transactionRepository;

  @Autowired
  private CategoryRepository categoryRepository;

  @BeforeEach
  void setUp() {
    transactionRepository.deleteAll();
  }

  @Test
  void shouldCreateTransactionSuccessfully() throws Exception {
    TransactionCreateDTO dto = TransactionCreateDTO.builder()
                                .amount(BigDecimal.valueOf(50.00))
                                .categoryId(1L)
                                .type(TransactionType.EXPENSE)
                                .date(LocalDate.of(2025, 6, 3))
                                .description("food")
                                .build();
    
    mockMvc.perform(post("/api/transactions")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(dto)))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.amount").value(50.00))
          .andExpect(jsonPath("$.description").value("food"));

  }

  @Test
  void shouldReturnAllTransactions() throws Exception {
    Category category = Category.builder()
                          .name("Groceries")
                          .type(TransactionType.EXPENSE)
                          .build();
    
    categoryRepository.save(category);

    Transaction transaction1 = Transaction.builder()
                                .amount(BigDecimal.valueOf(100.00))
                                .category(category)
                                .type(category.getType())
                                .date(LocalDate.of(2025, 6, 12))
                                .description("Fry's")
                                .build();

    Transaction transaction2 = Transaction.builder()
                                .amount(BigDecimal.valueOf(50.00))
                                .category(category)
                                .type(category.getType())
                                .date(LocalDate.of(2025, 6, 4))
                                .description("Whole Foods")
                                .build();

    transactionRepository.saveAll(List.of(transaction1, transaction2));

    mockMvc.perform(get("/api/transactions"))
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
  void shouldReturnFilteredTransactions() throws Exception {
    Category category = Category.builder()
                          .name("Groceries")
                          .type(TransactionType.EXPENSE)
                          .build();
    
    categoryRepository.save(category);

    Transaction transaction1 = Transaction.builder()
                                .amount(BigDecimal.valueOf(100.00))
                                .category(category)
                                .type(category.getType())
                                .date(LocalDate.of(2025, 6, 12))
                                .description("Fry's")
                                .build();

    Transaction transaction2 = Transaction.builder()
                                .amount(BigDecimal.valueOf(50.00))
                                .category(category)
                                .type(category.getType())
                                .date(LocalDate.of(2025, 6, 4))
                                .description("Whole Foods")
                                .build();

    Transaction transaction3 = Transaction.builder()
                                .amount(BigDecimal.valueOf(25.00))
                                .category(category)
                                .type(category.getType())
                                .date(LocalDate.of(2024, 11, 23))
                                .description("Target")
                                .build();

    transactionRepository.saveAll(List.of(transaction1, transaction2, transaction3));

    mockMvc.perform(get("/api/transactions/filter")
            .param("startDate", "2025-06-01"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.length()").value(2))
          .andExpect(jsonPath("$[0].date").value("2025-06-12"))
          .andExpect(jsonPath("$[1].date").value("2025-06-04"));
  }
}
