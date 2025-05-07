package com.stephenlindstrom.financeapp.budget_tool.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.stephenlindstrom.financeapp.budget_tool.dto.CategoryCreateDTO;
import com.stephenlindstrom.financeapp.budget_tool.dto.CategoryDTO;
import com.stephenlindstrom.financeapp.budget_tool.enums.TransactionType;
import com.stephenlindstrom.financeapp.budget_tool.model.Category;
import com.stephenlindstrom.financeapp.budget_tool.repository.CategoryRepository;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceImplTest {
  @Mock
  private CategoryRepository categoryRepository;

  private CategoryServiceImpl categoryService;

  @BeforeEach
  void setUp() {
    categoryService = new CategoryServiceImpl(categoryRepository);
  }

  @Test
  void testCreate_WithValidInput_ReturnsCategoryDTO() {
    // Arrange
    CategoryCreateDTO dto = new CategoryCreateDTO();
    dto.setName("Groceries");
    dto.setType(TransactionType.EXPENSE);

    Category savedCategory = Category.builder()
            .id(1L)
            .name("Groceries")
            .type(TransactionType.EXPENSE)
            .build();
    
    when(categoryRepository.save(any(Category.class))).thenReturn(savedCategory);

    // Act
    CategoryDTO result = categoryService.create(dto);

    // Assert
    assertEquals("Groceries", result.getName());
    assertEquals(TransactionType.EXPENSE, result.getType());
    assertEquals(1L, result.getId());
  }

  @Test
  void testCreate_WhenRepositoryFails_ThrowsException() {
    // Arrange
    CategoryCreateDTO dto = new CategoryCreateDTO();
    dto.setName("Groceries");
    dto.setType(TransactionType.EXPENSE);

    when(categoryRepository.save(any(Category.class)))
        .thenThrow(new RuntimeException("Database error"));
    
    // Act & Assert
    RuntimeException exception = assertThrows(RuntimeException.class, () -> {
      categoryService.create(dto);
    });

    assertEquals("Database error", exception.getMessage());
  }
  
  @Test
  void testGetAll_WithExistingEntries_ReturnsListOfCategoryDTO() {
    // Arrange
    Category savedCategory1 = Category.builder()
            .id(1L)
            .name("Groceries")
            .type(TransactionType.EXPENSE)
            .build();
    
    
    Category savedCategory2 = Category.builder()
            .id(2L)
            .name("Rent")
            .type(TransactionType.EXPENSE)
            .build();

    List<Category> categoryList = new ArrayList<>(Arrays.asList(savedCategory1, savedCategory2));

    when(categoryRepository.findAll()).thenReturn(categoryList);

    // Act
    List<CategoryDTO> result = categoryService.getAll();

    // Assert
    assertEquals(2, result.size());
    assertEquals("Rent", result.get(1).getName());
    assertEquals(TransactionType.EXPENSE, result.get(1).getType());
  }
}
