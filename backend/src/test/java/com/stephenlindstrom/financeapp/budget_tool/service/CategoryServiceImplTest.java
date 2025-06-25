package com.stephenlindstrom.financeapp.budget_tool.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import com.stephenlindstrom.financeapp.budget_tool.dto.CategoryCreateDTO;
import com.stephenlindstrom.financeapp.budget_tool.dto.CategoryDTO;
import com.stephenlindstrom.financeapp.budget_tool.enums.TransactionType;
import com.stephenlindstrom.financeapp.budget_tool.errors.ResourceNotFoundException;
import com.stephenlindstrom.financeapp.budget_tool.model.Category;
import com.stephenlindstrom.financeapp.budget_tool.repository.CategoryRepository;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceImplTest {
  @Mock
  private CategoryRepository categoryRepository;

  @InjectMocks
  private CategoryServiceImpl categoryService;

  @Test
  void testCreate_WithValidInput_ReturnsCategoryDTO() {
    // Arrange
    CategoryCreateDTO dto = CategoryCreateDTO.builder()
                              .name("Groceries")
                              .type(TransactionType.EXPENSE)
                              .build();

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
    CategoryCreateDTO dto = CategoryCreateDTO.builder()
                              .name("Groceries")
                              .type(TransactionType.EXPENSE)
                              .build();

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

    when(categoryRepository.findAll(any(Sort.class))).thenReturn(categoryList);

    // Act
    List<CategoryDTO> result = categoryService.getAll();

    // Assert
    assertEquals(2, result.size());
    assertEquals("Rent", result.get(1).getName());
    assertEquals(TransactionType.EXPENSE, result.get(1).getType());
  }

  @Test 
  void testGetAll_WithNoEntries_ReturnsEmptyList() {
    // Arrange
    List<Category> categoryList = new ArrayList<>();

    when(categoryRepository.findAll(any(Sort.class))).thenReturn(categoryList);

    // Act
    List<CategoryDTO> result = categoryService.getAll();

    // Assert
    assertTrue(result.isEmpty());
  }

  @Test
  void testGetById_IdExists_ReturnsOptionalCategoryDTO() {
    // Arrange
    Category savedCategory = Category.builder()
            .id(1L)
            .name("Groceries")
            .type(TransactionType.EXPENSE)
            .build();

    when(categoryRepository.findById(1L)).thenReturn(Optional.of(savedCategory));

    // Act
    Optional<CategoryDTO> result = categoryService.getById(1L);
    CategoryDTO dto = result.orElseThrow();

    // Assert
    assertEquals(1L, dto.getId());
    assertEquals("Groceries", dto.getName());
    assertEquals(TransactionType.EXPENSE, dto.getType());
  }

  @Test 
  void testGetById_IdDoesNotExist_ReturnsOptionalEmpty() {
    // Arrange
    when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

    // Act
    Optional<CategoryDTO> result = categoryService.getById(1L);

    // Assert
    assertTrue(result.isEmpty());
  }

  @Test
  void testUpdateById_CategoryExists_ReturnsCategoryDTO() {
    // Arrange
    Category existingCategory = Category.builder()
            .id(1L)
            .name("Groceries")
            .type(TransactionType.EXPENSE)
            .build();
    
    Category updatedCategory = Category.builder()
            .id(1L)
            .name("Salary")
            .type(TransactionType.INCOME)
            .build();

    CategoryCreateDTO dto = CategoryCreateDTO.builder()
            .name("Salary")
            .type(TransactionType.INCOME)
            .build();

    when(categoryRepository.findById(1L)).thenReturn(Optional.of(existingCategory));
    when(categoryRepository.save(any(Category.class))).thenReturn(updatedCategory);

    // Act
    CategoryDTO result = categoryService.updateById(1L, dto);

    // Assert
    assertEquals(1L, result.getId());
    assertEquals("Salary", result.getName());
    assertEquals(TransactionType.INCOME, result.getType());

    verify(categoryRepository).save(any(Category.class));
  }

  @Test
  void testUpdateById_CategoryDoesNotExist_ThrowsResourceNotFoundException() {
    // Arrange
    CategoryCreateDTO dto = CategoryCreateDTO.builder()
            .name("Salary")
            .type(TransactionType.INCOME)
            .build();

    when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

    // Act and Assert
    ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
      categoryService.updateById(1L, dto);
    });

    assertEquals("Category not found", exception.getMessage());

    verify(categoryRepository, never()).save(any(Category.class));
  }

  @Test
  void testDeleteById_IdExists_NoReturnValue() {
    // Act
    categoryService.deleteById(1L);

    // Assert
    verify(categoryRepository).deleteById(1L);
  }

  @Test
  void testExistsByNameIgnoreCase_NameExists_ReturnsTrue() {
    // Arrange
    when(categoryRepository.existsByNameIgnoreCase("Groceries")).thenReturn(true);

    // Act and Assert
    assertTrue(categoryService.existsByNameIgnoreCase("Groceries"));
  }

  @Test
  void testExistsByNameIgnoreCase_NameDoesNotExist_ReturnsFalse() {
    // Arrange
    when(categoryRepository.existsByNameIgnoreCase("Groceries")).thenReturn(false);

    // Act and Assert
    assertFalse(categoryService.existsByNameIgnoreCase("Groceries"));
  }

  @Test
  void testGetByType_WithMatchingEntries_ReturnsListOfCategoryDTO() {
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

    Category savedCategory3 = Category.builder()
            .id(3L)
            .name("Salary")
            .type(TransactionType.INCOME)
            .build();

    List<Category> categoryList = new ArrayList<>(Arrays.asList(savedCategory1, savedCategory2, savedCategory3));

    when(categoryRepository.findAll()).thenReturn(categoryList);

    // Act
    List<CategoryDTO> result = categoryService.getByType(TransactionType.EXPENSE);

    // Assert
    assertEquals(2, result.size());
    assertEquals(TransactionType.EXPENSE, result.get(0).getType());
    assertEquals(TransactionType.EXPENSE, result.get(1).getType());
  }

  @Test
  void testGetByType_WithoutMatchingEntries_ReturnsEmptyList() {
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
    List<CategoryDTO> result = categoryService.getByType(TransactionType.INCOME);

    // Assert
    assertTrue(result.isEmpty());
  }
}
