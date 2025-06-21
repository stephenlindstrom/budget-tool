package com.stephenlindstrom.financeapp.budget_tool.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
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

import com.stephenlindstrom.financeapp.budget_tool.dto.BudgetCreateDTO;
import com.stephenlindstrom.financeapp.budget_tool.dto.BudgetDTO;
import com.stephenlindstrom.financeapp.budget_tool.dto.BudgetSummaryDTO;
import com.stephenlindstrom.financeapp.budget_tool.dto.CategoryDTO;
import com.stephenlindstrom.financeapp.budget_tool.dto.MonthDTO;
import com.stephenlindstrom.financeapp.budget_tool.dto.TransactionDTO;
import com.stephenlindstrom.financeapp.budget_tool.dto.TransactionFilter;
import com.stephenlindstrom.financeapp.budget_tool.enums.TransactionType;
import com.stephenlindstrom.financeapp.budget_tool.errors.ResourceNotFoundException;
import com.stephenlindstrom.financeapp.budget_tool.model.Budget;
import com.stephenlindstrom.financeapp.budget_tool.model.Category;
import com.stephenlindstrom.financeapp.budget_tool.repository.BudgetRepository;
import com.stephenlindstrom.financeapp.budget_tool.repository.CategoryRepository;

@ExtendWith(MockitoExtension.class)
public class BudgetServiceImplTest {
  @Mock
  private BudgetRepository budgetRepository;

  @Mock
  private CategoryRepository categoryRepository;

  @Mock
  private TransactionService transactionService;

  @InjectMocks
  private BudgetServiceImpl budgetService;

  @Test
  void testCreate_WithValidInput_ReturnsBudgetDTO() {
    // Arrange
    BudgetCreateDTO dto = BudgetCreateDTO.builder()
                            .value(BigDecimal.valueOf(500.00))
                            .month(YearMonth.of(2025, 5))
                            .categoryId(1L)
                            .build();

    Category savedCategory = Category.builder()
            .id(1L)
            .name("Groceries")
            .type(TransactionType.EXPENSE)
            .build();
    
    CategoryDTO categoryDTO = CategoryDTO.builder()
                .id(1L)
                .name("Groceries")
                .type(TransactionType.EXPENSE)
                .build();

    Budget savedBudget = Budget.builder()
            .id(1L)
            .value(BigDecimal.valueOf(500.00))
            .month(YearMonth.of(2025, 5))
            .category(savedCategory)
            .build();

    when(budgetRepository.save(any(Budget.class))).thenReturn(savedBudget);
    when(categoryRepository.findById(1L)).thenReturn(Optional.of(savedCategory));

    // Act
    BudgetDTO result = budgetService.create(dto);

    // Assert
    assertEquals(1, result.getId());
    assertEquals(BigDecimal.valueOf(500.00), result.getValue());
    assertEquals(YearMonth.of(2025, 5), result.getMonth());
    assertEquals(categoryDTO.getId(), result.getCategory().getId());
    assertEquals(categoryDTO.getName(), result.getCategory().getName());
    assertEquals(categoryDTO.getType(), result.getCategory().getType());
  }

  @Test
  void testGetAll_WithEntries_ReturnsListOfBudgetDTO() {
    // Arrange
    Category savedCategory = Category.builder()
            .id(1L)
            .name("Groceries")
            .type(TransactionType.EXPENSE)
            .build();

    Budget savedBudget1 = Budget.builder()
          .id(1L)
          .value(BigDecimal.valueOf(500.00))
          .month(YearMonth.of(2025, 5))
          .category(savedCategory)
          .build();

    Budget savedBudget2 = Budget.builder()
          .id(2L)
          .value(BigDecimal.valueOf(300.00))
          .month(YearMonth.of(2025, 4))
          .category(savedCategory)
          .build();

    List<Budget> budgetList = new ArrayList<>(Arrays.asList(savedBudget1, savedBudget2));

    when(budgetRepository.findAll(any(Sort.class))).thenReturn(budgetList);

    // Act
    List<BudgetDTO> dtos = budgetService.getAll();

    // Assert
    assertEquals(2, dtos.size());
    assertEquals(1L, dtos.get(0).getId());
    assertEquals(2L, dtos.get(1).getId());
    assertEquals(BigDecimal.valueOf(500.00), dtos.get(0).getValue());
    assertEquals(BigDecimal.valueOf(300.00), dtos.get(1).getValue());
    assertEquals(YearMonth.of(2025, 5), dtos.get(0).getMonth());
    assertEquals(YearMonth.of(2025, 4), dtos.get(1).getMonth());
    assertEquals(1L, dtos.get(0).getCategory().getId());
    assertEquals(1L, dtos.get(1).getCategory().getId());
    assertEquals("Groceries", dtos.get(0).getCategory().getName());
    assertEquals("Groceries", dtos.get(1).getCategory().getName());
    assertEquals(TransactionType.EXPENSE, dtos.get(0).getCategory().getType());
    assertEquals(TransactionType.EXPENSE, dtos.get(1).getCategory().getType());
  }

  @Test
  void testGetAll_WithNoEntries_ReturnsEmptyList() {
    // Arrange
    List<Budget> budgetList = new ArrayList<>();

    when(budgetRepository.findAll(any(Sort.class))).thenReturn(budgetList);

    // Act
    List<BudgetDTO> dtos = budgetService.getAll();

    // Assert
    assertTrue(dtos.isEmpty());
  }

  @Test
  void testGetById_IdExists_ReturnsOptionalBudgetDTO() {
    // Arrange
    Category savedCategory = Category.builder()
            .id(1L)
            .name("Groceries")
            .type(TransactionType.EXPENSE)
            .build();

    Budget savedBudget = Budget.builder()
          .id(1L)
          .value(BigDecimal.valueOf(500.00))
          .month(YearMonth.of(2025, 5))
          .category(savedCategory)
          .build();


    when(budgetRepository.findById(1L)).thenReturn(Optional.of(savedBudget));
    
    // Act
    Optional<BudgetDTO> result = budgetService.getById(1L);
    BudgetDTO dto = result.orElseThrow();

    // Assert
    assertEquals(1L, dto.getId());
    assertEquals(BigDecimal.valueOf(500.00), dto.getValue());
    assertEquals(YearMonth.of(2025, 5), dto.getMonth());
    assertEquals(1L, dto.getCategory().getId());
    assertEquals("Groceries", dto.getCategory().getName());
    assertEquals(TransactionType.EXPENSE, dto.getCategory().getType());
  }

  @Test
  void testGetById_IdDoesNotExist_ReturnsOptionalEmpty() {
    // Arrange
    when(budgetRepository.findById(1L)).thenReturn(Optional.empty());

    // Act
    Optional<BudgetDTO> result = budgetService.getById(1L);

    // Assert
    assertTrue(result.isEmpty());
  }

  @Test
  void testDeleteById_WithValidId_NoReturnValue() {
    // Act
    budgetService.deleteById(1L);

    // Assert
    verify(budgetRepository).deleteById(1L);
  }

  @Test
  void testExistsByCategoryIdAndMonth_ValidInput_ReturnsTrue() {
    // Arrange
    when(budgetRepository.existsByCategoryIdAndMonth(1L, YearMonth.of(2025, 5))).thenReturn(true);

    // Act and Assert
    assertTrue(budgetService.existsByCategoryIdAndMonth(1L, YearMonth.of(2025, 5)));
  }

  @Test
  void testExistsByCategoryIdAndMonth_InvalidInput_ReturnsFalse() {
     // Arrange
    when(budgetRepository.existsByCategoryIdAndMonth(1L, YearMonth.of(2025, 5))).thenReturn(false);

    // Act and Assert
    assertFalse(budgetService.existsByCategoryIdAndMonth(1L, YearMonth.of(2025, 5)));
  }

  @Test
  void testGetBudgetSummary_WithValidId_ReturnsBudgetSummaryDTO() {
    // Arrange
    Category savedCategory = Category.builder()
            .id(1L)
            .name("Groceries")
            .type(TransactionType.EXPENSE)
            .build();
    
    CategoryDTO savedCategoryDTO = CategoryDTO.builder()
              .id(1L)
              .name("Groceries")
              .type(TransactionType.EXPENSE)
              .build();

    TransactionDTO savedTransactionDTO1 = TransactionDTO.builder()
              .id(1L)
              .amount(BigDecimal.valueOf(100.00))
              .category(savedCategoryDTO)
              .type(TransactionType.EXPENSE)
              .date(LocalDate.of(2025, 5, 1))
              .description("Food")
              .build();

    TransactionDTO savedTransactionDTO2 = TransactionDTO.builder()
              .id(2L)
              .amount(BigDecimal.valueOf(50.00))
              .category(savedCategoryDTO)
              .type(TransactionType.EXPENSE)
              .date(LocalDate.of(2025, 5, 31))
              .description("Drinks")
              .build();
            
    List<TransactionDTO> transactionDTOs = new ArrayList<>(Arrays.asList(savedTransactionDTO1, savedTransactionDTO2));

    Budget savedBudget = Budget.builder()
          .id(1L)
          .value(BigDecimal.valueOf(500.00))
          .month(YearMonth.of(2025, 5))
          .category(savedCategory)
          .build();

    when(budgetRepository.findById(1L)).thenReturn(Optional.of(savedBudget));
    when(transactionService.filter(any(TransactionFilter.class))).thenReturn(transactionDTOs);

    // Act
    BudgetSummaryDTO result = budgetService.getBudgetSummary(1L);

    // Assert
    assertEquals(BigDecimal.valueOf(500.00), result.getBudgeted());
    assertEquals(BigDecimal.valueOf(150.00), result.getSpent());
    assertEquals(BigDecimal.valueOf(350.00), result.getRemaining());
  }

  @Test
  void testGetBudgetSummary_WithInvalidId_ThrowsResourceNotFoundException() {
    // Arrange
    when(budgetRepository.findById(1L)).thenReturn(Optional.empty());

    // Act and Assert
    ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
      budgetService.getBudgetSummary(1L);
    });

    assertEquals("Budget not found", exception.getMessage());
  }

  @Test
  void testGetByMonth_WithEntries_ReturnsListOfBudgetDTOs() {
    // Arrange
    Category savedCategory = Category.builder()
            .id(1L)
            .name("Groceries")
            .type(TransactionType.EXPENSE)
            .build();

    Budget savedBudget1 = Budget.builder()
          .id(1L)
          .value(BigDecimal.valueOf(500.00))
          .month(YearMonth.of(2025, 5))
          .category(savedCategory)
          .build();

    Budget savedBudget2 = Budget.builder()
          .id(2L)
          .value(BigDecimal.valueOf(300.00))
          .month(YearMonth.of(2025, 5))
          .category(savedCategory)
          .build();

    List<Budget> budgetList = new ArrayList<>(Arrays.asList(savedBudget1, savedBudget2));
    when(budgetRepository.findByMonth(YearMonth.of(2025, 5))).thenReturn(budgetList);

    // Act
    List<BudgetDTO> result = budgetService.getByMonth(YearMonth.of(2025, 5));

    // Assert 
    assertEquals(2, result.size());
    assertEquals(1L, result.get(0).getId());
    assertEquals(2L, result.get(1).getId());
    assertEquals(BigDecimal.valueOf(500.00), result.get(0).getValue());
    assertEquals(BigDecimal.valueOf(300.00), result.get(1).getValue());
    assertEquals(YearMonth.of(2025, 5), result.get(0).getMonth());
    assertEquals(YearMonth.of(2025, 5), result.get(1).getMonth());
    assertEquals(1L, result.get(0).getCategory().getId());
    assertEquals(1L, result.get(1).getCategory().getId());
    assertEquals("Groceries", result.get(0).getCategory().getName());
    assertEquals("Groceries", result.get(1).getCategory().getName());
    assertEquals(TransactionType.EXPENSE, result.get(0).getCategory().getType());
    assertEquals(TransactionType.EXPENSE, result.get(1).getCategory().getType());
  }

  @Test
  void testGetByMonth_WithoutEntries_ReturnsEmptyList() {
    // Arrange
    List<Budget> budgetList = new ArrayList<>();
    when(budgetRepository.findByMonth(YearMonth.of(2025, 5))).thenReturn(budgetList);

    // Act
    List<BudgetDTO> result = budgetService.getByMonth(YearMonth.of(2025, 5));

    // Assert
    assertTrue(result.isEmpty());
  }

  @Test 
  void testGetAvailableMonths_WithEntries_ReturnsListOfMonthDTOs() {
    // Arrange
    when(budgetRepository.findDistinctMonths()).thenReturn(List.of(YearMonth.of(2025, 3), YearMonth.of(2025, 4), YearMonth.of(2025,5)));

    // Act
    List<MonthDTO> result = budgetService.getAvailableMonths();

    // Assert
    assertEquals(3, result.size());
    assertEquals("2025-05", result.get(0).getValue());
    assertEquals("May 2025", result.get(0).getDisplay());
    assertEquals("2025-04", result.get(1).getValue());
    assertEquals("April 2025", result.get(1).getDisplay());
    assertEquals("2025-03", result.get(2).getValue());
    assertEquals("March 2025", result.get(2).getDisplay());
  }

  @Test
  void testGetAvailableMonths_WithoutEntries_ReturnsEmptyList() {
    // Arrange
    List<YearMonth> months = new ArrayList<>();
    when(budgetRepository.findDistinctMonths()).thenReturn(months);

    // Act
    List<MonthDTO> result = budgetService.getAvailableMonths();

    // Assert
    assertTrue(result.isEmpty());
  }
}
