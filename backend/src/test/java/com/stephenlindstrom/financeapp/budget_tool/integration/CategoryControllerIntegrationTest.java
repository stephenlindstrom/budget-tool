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
import org.springframework.http.MediaType;

import com.stephenlindstrom.financeapp.budget_tool.dto.CategoryCreateDTO;
import com.stephenlindstrom.financeapp.budget_tool.enums.TransactionType;
import com.stephenlindstrom.financeapp.budget_tool.model.Category;
import com.stephenlindstrom.financeapp.budget_tool.model.User;
import com.stephenlindstrom.financeapp.budget_tool.repository.CategoryRepository;

public class CategoryControllerIntegrationTest extends AbstractIntegrationTest {

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
            .with(bearerToken())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(dto)))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.name").value("Groceries"))
          .andExpect(jsonPath("$.type").value("EXPENSE"));
  }

  @Test
  void shouldReturn400WhenCreatingCategoryWithInvalidInput() throws Exception {
    CategoryCreateDTO dto = CategoryCreateDTO.builder()
                              .name(null)
                              .type(null)
                              .build();

    mockMvc.perform(post("/api/categories")
              .with(bearerToken())
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isBadRequest());
  }

  @Test
  void shouldReturnAllCategories() throws Exception {
    Category category1 = Category.builder()
                          .name("Groceries")
                          .type(TransactionType.EXPENSE)
                          .user(testUser)
                          .build();

    Category category2 = Category.builder()
                          .name("Salary")
                          .type(TransactionType.INCOME)
                          .user(testUser)
                          .build();

    categoryRepository.saveAll(List.of(category1, category2));

    mockMvc.perform(get("/api/categories")
            .with(bearerToken()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].name").value("Groceries"))
            .andExpect(jsonPath("$[0].type").value("EXPENSE"))
            .andExpect(jsonPath("$[1].name").value("Salary"))
            .andExpect(jsonPath("$[1].type").value("INCOME"));
  }

  @Test
  void shouldOnlyReturnCurrentUserCategories() throws Exception {
    User anotherUser = userRepository.save(
      User.builder().username("anotherUser").password("hashedPassword").build()
    ); 

    Category category1 = Category.builder()
                          .name("Groceries")
                          .type(TransactionType.EXPENSE)
                          .user(testUser)
                          .build();

    Category category2 = Category.builder()
                          .name("Salary")
                          .type(TransactionType.INCOME)
                          .user(testUser)
                          .build();

    Category category3 = Category.builder()
                          .name("Car")
                          .type(TransactionType.EXPENSE)
                          .user(anotherUser)
                          .build();

    categoryRepository.saveAll(List.of(category1, category2, category3));

    mockMvc.perform(get("/api/categories")
            .with(bearerToken()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].name").value("Groceries"))
            .andExpect(jsonPath("$[1].name").value("Salary"))
            .andExpect(jsonPath("$[?(@.name == 'Car')]").doesNotExist());
  }

  @Test 
  void shouldReturnCategoryById() throws Exception {
    Category category = categoryRepository.save(Category.builder()
        .name("Groceries")
        .type(TransactionType.EXPENSE)
        .user(testUser)
        .build()
    );

    mockMvc.perform(get("/api/categories/{id}", category.getId())
          .with(bearerToken()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id").value(category.getId()))
          .andExpect(jsonPath("$.name").value("Groceries"))
          .andExpect(jsonPath("$.type").value("EXPENSE"));
  }

  @Test
  void shouldReturn404WhenGettingNonExistentCategory() throws Exception {
    mockMvc.perform(get("/api/categories/{id}", 999L)
            .with(bearerToken()))
            .andExpect(status().isNotFound());
  }

  @Test
  void shouldReturn404WhenAccessingAnotherUsersCategoryById() throws Exception {
    User anotherUser = userRepository.save(
      User.builder().username("anotherUser").password("hashedPassword").build()
    );

    Category category = categoryRepository.save(Category.builder()
                          .name("Car")
                          .type(TransactionType.EXPENSE)
                          .user(anotherUser)
                          .build()
    );

    mockMvc.perform(get("/api/categories/{id}", category.getId())
                    .with(bearerToken()))
                    .andExpect(status().isNotFound());
  }

  @Test
  void shouldUpdateCategoryById() throws Exception {
    Category category = categoryRepository.save(Category.builder()
        .name("Groceries")
        .type(TransactionType.EXPENSE)
        .user(testUser)
        .build()
    );

    CategoryCreateDTO dto = CategoryCreateDTO.builder()
                              .name("Salary")
                              .type(TransactionType.INCOME)
                              .build();

    mockMvc.perform(put("/api/categories/{id}", category.getId())
              .with(bearerToken())
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(category.getId()))
            .andExpect(jsonPath("$.name").value("Salary"))
            .andExpect(jsonPath("$.type").value("INCOME"));
  }

  @Test
  void shouldReturn404WhenUpdatingNonExistentCategory() throws Exception {
    CategoryCreateDTO dto = CategoryCreateDTO.builder()
                              .name("Salary")
                              .type(TransactionType.INCOME)
                              .build();

    mockMvc.perform(put("/api/categories/{id}", 999L)
              .with(bearerToken())
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isNotFound()); 
  }

  @Test
  void shouldDeleteCategoryByIdAndReturnNoContent() throws Exception {
    Category category = categoryRepository.save(Category.builder()
        .name("Groceries")
        .type(TransactionType.EXPENSE)
        .user(testUser)
        .build()
    );

    mockMvc.perform(delete("/api/categories/{id}", category.getId())
          .with(bearerToken()))
          .andExpect(status().isNoContent());
    
    assertFalse(categoryRepository.findByIdAndUser(category.getId(), testUser).isPresent());
  }

  @Test
  void shouldReturnNoContentWhenDeletingNonExistentCategory() throws Exception {
    mockMvc.perform(delete("/api/categories/{id}", 999L)
            .with(bearerToken()))
            .andExpect(status().isNoContent());
  }
}
