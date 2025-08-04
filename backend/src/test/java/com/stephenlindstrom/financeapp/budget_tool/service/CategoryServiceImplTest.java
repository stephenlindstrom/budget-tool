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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.stephenlindstrom.financeapp.budget_tool.dto.CategoryCreateDTO;
import com.stephenlindstrom.financeapp.budget_tool.dto.CategoryDTO;
import com.stephenlindstrom.financeapp.budget_tool.enums.TransactionType;
import com.stephenlindstrom.financeapp.budget_tool.errors.ResourceNotFoundException;
import com.stephenlindstrom.financeapp.budget_tool.model.Category;
import com.stephenlindstrom.financeapp.budget_tool.model.User;
import com.stephenlindstrom.financeapp.budget_tool.repository.CategoryRepository;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceImplTest {
  @Mock
  private CategoryRepository categoryRepository;

  @Mock 
  private UserService userService;

  @InjectMocks
  private CategoryServiceImpl categoryService;

  private User mockUser;

  @Captor
  private ArgumentCaptor<Category> categoryCaptor;

  @BeforeEach
  void setup() {
    mockUser = User.builder().id(1L).username("mockUser").build();
    when(userService.getAuthenticatedUser()).thenReturn(mockUser);
  }

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
            .user(mockUser)
            .build();
    
    when(categoryRepository.save(categoryCaptor.capture())).thenReturn(savedCategory);

    // Act
    CategoryDTO result = categoryService.create(dto);

    // Assert
    assertEquals("Groceries", result.getName());
    assertEquals(TransactionType.EXPENSE, result.getType());
    assertEquals(1L, result.getId());
    assertEquals(mockUser, categoryCaptor.getValue().getUser());

    verify(userService).getAuthenticatedUser();
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
            .user(mockUser)
            .build();
    
    
    Category savedCategory2 = Category.builder()
            .id(2L)
            .name("Rent")
            .type(TransactionType.EXPENSE)
            .user(mockUser)
            .build();

    List<Category> categoryList = new ArrayList<>(Arrays.asList(savedCategory1, savedCategory2));

    when(categoryRepository.findByUserOrderByName(mockUser)).thenReturn(categoryList);

    // Act
    List<CategoryDTO> result = categoryService.getAll();

    // Assert
    assertEquals(2, result.size());
    assertEquals("Rent", result.get(1).getName());
    assertEquals(TransactionType.EXPENSE, result.get(1).getType());

    verify(userService).getAuthenticatedUser();
  }

  @Test 
  void testGetAll_WithNoEntries_ReturnsEmptyList() {
    // Arrange
    when(categoryRepository.findByUserOrderByName(mockUser)).thenReturn(Collections.emptyList());

    // Act
    List<CategoryDTO> result = categoryService.getAll();

    // Assert
    assertTrue(result.isEmpty());
    verify(userService).getAuthenticatedUser();
  }

  @Test
  void testGetById_IdExists_ReturnsOptionalCategoryDTO() {
    // Arrange
    Category savedCategory = Category.builder()
            .id(1L)
            .name("Groceries")
            .type(TransactionType.EXPENSE)
            .user(mockUser)
            .build();

    when(categoryRepository.findByIdAndUser(1L, mockUser)).thenReturn(Optional.of(savedCategory));

    // Act
    Optional<CategoryDTO> result = categoryService.getById(1L);
    CategoryDTO dto = result.orElseThrow();

    // Assert
    assertEquals(1L, dto.getId());
    assertEquals("Groceries", dto.getName());
    assertEquals(TransactionType.EXPENSE, dto.getType());

    verify(userService).getAuthenticatedUser();
  }

  @Test 
  void testGetById_IdDoesNotExist_ReturnsOptionalEmpty() {
    // Arrange
    when(categoryRepository.findByIdAndUser(1L, mockUser)).thenReturn(Optional.empty());

    // Act
    Optional<CategoryDTO> result = categoryService.getById(1L);

    // Assert
    assertTrue(result.isEmpty());
    verify(userService).getAuthenticatedUser();
  }

  @Test
  void testUpdateById_CategoryExists_ReturnsCategoryDTO() {
    // Arrange
    Category existingCategory = Category.builder()
            .id(1L)
            .name("Groceries")
            .type(TransactionType.EXPENSE)
            .user(mockUser)
            .build();
    
    Category updatedCategory = Category.builder()
            .id(1L)
            .name("Salary")
            .type(TransactionType.INCOME)
            .user(mockUser)
            .build();

    CategoryCreateDTO dto = CategoryCreateDTO.builder()
            .name("Salary")
            .type(TransactionType.INCOME)
            .build();

    when(categoryRepository.findByIdAndUser(1L, mockUser)).thenReturn(Optional.of(existingCategory));
    when(categoryRepository.save(any(Category.class))).thenReturn(updatedCategory);

    // Act
    CategoryDTO result = categoryService.updateById(1L, dto);

    // Assert
    assertEquals(1L, result.getId());
    assertEquals("Salary", result.getName());
    assertEquals(TransactionType.INCOME, result.getType());

    verify(categoryRepository).save(any(Category.class));
    verify(userService).getAuthenticatedUser();
  }

  @Test
  void testUpdateById_CategoryDoesNotExist_ThrowsResourceNotFoundException() {
    // Arrange
    CategoryCreateDTO dto = CategoryCreateDTO.builder()
            .name("Salary")
            .type(TransactionType.INCOME)
            .build();

    when(categoryRepository.findByIdAndUser(1L, mockUser)).thenReturn(Optional.empty());

    // Act and Assert
    ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
      categoryService.updateById(1L, dto);
    });

    assertEquals("Category not found", exception.getMessage());

    verify(categoryRepository, never()).save(any(Category.class));
    verify(userService).getAuthenticatedUser();
  }

  @Test
  void testDeleteById_IdExists_DeletesCategoryForUser() {
    // Act
    categoryService.deleteById(1L);

    // Assert
    verify(userService).getAuthenticatedUser();
    verify(categoryRepository).deleteByIdAndUser(1L, mockUser);
  }

  @Test
  void testExistsByNameIgnoreCase_NameExists_ReturnsTrue() {
    // Arrange
    when(categoryRepository.existsByNameIgnoreCaseAndUser("Groceries", mockUser)).thenReturn(true);

    // Act and Assert
    assertTrue(categoryService.existsByNameIgnoreCase("Groceries"));
    verify(userService).getAuthenticatedUser();
  }

  @Test
  void testExistsByNameIgnoreCase_NameDoesNotExist_ReturnsFalse() {
    // Arrange
    when(categoryRepository.existsByNameIgnoreCaseAndUser("Groceries", mockUser)).thenReturn(false);

    // Act and Assert
    assertFalse(categoryService.existsByNameIgnoreCase("Groceries"));
    verify(userService).getAuthenticatedUser();
  }

  @Test
  void testGetByType_WithMatchingEntries_ReturnsListOfCategoryDTO() {
    // Arrange
    Category savedCategory1 = Category.builder()
            .id(1L)
            .name("Groceries")
            .type(TransactionType.EXPENSE)
            .user(mockUser)
            .build();
    
    
    Category savedCategory2 = Category.builder()
            .id(2L)
            .name("Rent")
            .type(TransactionType.EXPENSE)
            .user(mockUser)
            .build();

    Category savedCategory3 = Category.builder()
            .id(3L)
            .name("Salary")
            .type(TransactionType.INCOME)
            .user(mockUser)
            .build();

    List<Category> categoryList = new ArrayList<>(Arrays.asList(savedCategory1, savedCategory2, savedCategory3));

    when(categoryRepository.findByUserOrderByName(mockUser)).thenReturn(categoryList);

    // Act
    List<CategoryDTO> result = categoryService.getByType(TransactionType.EXPENSE);

    // Assert
    assertEquals(2, result.size());
    assertEquals(TransactionType.EXPENSE, result.get(0).getType());
    assertEquals(TransactionType.EXPENSE, result.get(1).getType());

    verify(userService).getAuthenticatedUser();
  }

  @Test
  void testGetByType_WithoutMatchingEntries_ReturnsEmptyList() {
    // Arrange
    Category savedCategory1 = Category.builder()
            .id(1L)
            .name("Groceries")
            .type(TransactionType.EXPENSE)
            .user(mockUser)
            .build();
    
    
    Category savedCategory2 = Category.builder()
            .id(2L)
            .name("Rent")
            .type(TransactionType.EXPENSE)
            .user(mockUser)
            .build();

    List<Category> categoryList = new ArrayList<>(Arrays.asList(savedCategory1, savedCategory2));

    when(categoryRepository.findByUserOrderByName(mockUser)).thenReturn(categoryList);

    // Act
    List<CategoryDTO> result = categoryService.getByType(TransactionType.INCOME);

    // Assert
    assertTrue(result.isEmpty());
    verify(userService).getAuthenticatedUser();
  }
}
