package com.stephenlindstrom.financeapp.budget_tool.integration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stephenlindstrom.financeapp.budget_tool.dto.CategoryCreateDTO;
import com.stephenlindstrom.financeapp.budget_tool.enums.TransactionType;
import com.stephenlindstrom.financeapp.budget_tool.model.Category;
import com.stephenlindstrom.financeapp.budget_tool.repository.CategoryRepository;

@ActiveProfiles("test")
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

  @Test
  void shouldCreateCategorySuccessfully() throws Exception {
    CategoryCreateDTO dto = CategoryCreateDTO.builder()
                              .name("Groceries")
                              .type(TransactionType.EXPENSE)
                              .build();
    
    mockMvc.perform(post("/api/categories")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(dto)))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.name").value("Groceries"))
          .andExpect(jsonPath("$.type").value("EXPENSE"));
  }

  @Test
  void shouldReturnAllCategories() throws Exception {
    Category category1 = Category.builder()
                          .name("Groceries")
                          .type(TransactionType.EXPENSE)
                          .build();

    Category category2 = Category.builder()
                          .name("Salary")
                          .type(TransactionType.INCOME)
                          .build();

    categoryRepository.saveAll(List.of(category1, category2));

    mockMvc.perform(get("/api/categories"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].name").value("Groceries"))
            .andExpect(jsonPath("$[0].type").value("EXPENSE"))
            .andExpect(jsonPath("$[1].name").value("Salary"))
            .andExpect(jsonPath("$[1].type").value("INCOME"));
  }

  @Test 
  void shouldReturnCategoryById() throws Exception {
    Category category = categoryRepository.save(Category.builder()
        .name("Groceries")
        .type(TransactionType.EXPENSE)
        .build()
    );

    mockMvc.perform(get("/api/categories/{id}", category.getId()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id").value(category.getId()))
          .andExpect(jsonPath("$.name").value("Groceries"))
          .andExpect(jsonPath("$.type").value("EXPENSE"));
  }

  @Test
  void shouldUpdateCategoryById() throws Exception {
    Category category = categoryRepository.save(Category.builder()
        .name("Groceries")
        .type(TransactionType.EXPENSE)
        .build()
    );

    CategoryCreateDTO dto = CategoryCreateDTO.builder()
                              .name("Salary")
                              .type(TransactionType.INCOME)
                              .build();

    mockMvc.perform(put("/api/categories/{id}", category.getId())
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(category.getId()))
            .andExpect(jsonPath("$.name").value("Salary"))
            .andExpect(jsonPath("$.type").value("INCOME"));
  }

  @Test
  void shouldDeleteCategoryByIdAndReturnNoContent() throws Exception {
    Category category = categoryRepository.save(Category.builder()
        .name("Groceries")
        .type(TransactionType.EXPENSE)
        .build()
    );

    mockMvc.perform(delete("/api/categories/{id}", category.getId()))
          .andExpect(status().isNoContent());
    
    assertFalse(categoryRepository.findById(category.getId()).isPresent());
  }
}
