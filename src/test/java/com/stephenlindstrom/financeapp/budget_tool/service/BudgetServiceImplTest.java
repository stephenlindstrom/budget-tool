package com.stephenlindstrom.financeapp.budget_tool.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.stephenlindstrom.financeapp.budget_tool.dto.BudgetCreateDTO;
import com.stephenlindstrom.financeapp.budget_tool.dto.BudgetDTO;
import com.stephenlindstrom.financeapp.budget_tool.dto.CategoryDTO;
import com.stephenlindstrom.financeapp.budget_tool.enums.TransactionType;
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

  private BudgetServiceImpl budgetService;

  @BeforeEach
  void setUp() {
    budgetService = new BudgetServiceImpl(budgetRepository, categoryRepository, transactionService);
  }

  @Test
  void testCreate_WithValidInput_ReturnsBudgetDTO() {
    // Arrange
    BudgetCreateDTO dto = new BudgetCreateDTO();
    dto.setValue(BigDecimal.valueOf(500.00));
    dto.setMonth(YearMonth.of(2025, 5));
    dto.setCategoryId(1L);

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
}
