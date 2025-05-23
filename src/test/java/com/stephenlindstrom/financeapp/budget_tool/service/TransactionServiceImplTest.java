package com.stephenlindstrom.financeapp.budget_tool.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.stephenlindstrom.financeapp.budget_tool.dto.CategoryDTO;
import com.stephenlindstrom.financeapp.budget_tool.dto.TransactionCreateDTO;
import com.stephenlindstrom.financeapp.budget_tool.dto.TransactionDTO;
import com.stephenlindstrom.financeapp.budget_tool.enums.TransactionType;
import com.stephenlindstrom.financeapp.budget_tool.errors.ResourceNotFoundException;
import com.stephenlindstrom.financeapp.budget_tool.model.Category;
import com.stephenlindstrom.financeapp.budget_tool.model.Transaction;
import com.stephenlindstrom.financeapp.budget_tool.repository.CategoryRepository;
import com.stephenlindstrom.financeapp.budget_tool.repository.TransactionRepository;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceImplTest {
  @Mock
  private TransactionRepository transactionRepository;

  @Mock
  private CategoryRepository categoryRepository;

  @InjectMocks
  private TransactionServiceImpl transactionService;


  @Test
  void testSave_WithValidInput_ReturnsTransactionDTO() {
    // Arrange
    BigDecimal amount = BigDecimal.valueOf(100.00);
    Long categoryId = 1L;
    TransactionType type = TransactionType.EXPENSE;
    LocalDate date = LocalDate.of(2025, 5, 1);
    String description = "food";

    Category savedCategory = Category.builder()
        .id(categoryId)
        .name("Groceries")
        .type(type)
        .build();

    TransactionCreateDTO dto = TransactionCreateDTO.builder()
        .amount(amount)
        .categoryId(categoryId)
        .type(type)
        .date(date)
        .description(description)
        .build();

    Transaction savedTransaction = Transaction.builder()
        .id(1L)
        .amount(amount)
        .category(savedCategory)
        .type(type)
        .date(date)
        .description(description)
        .build();

    when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTransaction);
    when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(savedCategory));

    // Act
    TransactionDTO result = transactionService.save(dto);

    // Assert
    verify(transactionRepository).save(any(Transaction.class));

    assertEquals(1L, result.getId());
    assertEquals(amount, result.getAmount());
    assertEquals(type, result.getType());
    assertEquals(date, result.getDate());
    assertEquals(description, result.getDescription());

    CategoryDTO categoryDTO = result.getCategory();
    assertEquals(categoryId, categoryDTO.getId());
    assertEquals("Groceries", categoryDTO.getName());
    assertEquals(type, categoryDTO.getType());
  }

  @Test
  void testSave_WithInvalidCategoryId_ThrowsResourceNotFoundException() {
    // Arrange
    TransactionCreateDTO dto = TransactionCreateDTO.builder()
        .amount(BigDecimal.valueOf(100.00))
        .categoryId(1L)
        .type(TransactionType.EXPENSE)
        .date(LocalDate.of(2025, 5, 1))
        .description("food")
        .build();

    when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

    // Act
    ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
      transactionService.save(dto);
    });

    assertEquals("Category not found", exception.getMessage());
    verify(transactionRepository, never()).save(any(Transaction.class));
  }

  @Test
  void testSave_WithNullDateAndNullDescription_ReturnsTransactionDTOWithCurrentDateAndEmptyDescription() {
      // Arrange
    BigDecimal amount = BigDecimal.valueOf(100.00);
    Long categoryId = 1L;
    TransactionType type = TransactionType.EXPENSE;
    LocalDate now = LocalDate.now();

    Category savedCategory = Category.builder()
        .id(categoryId)
        .name("Groceries")
        .type(type)
        .build();

    TransactionCreateDTO dto = TransactionCreateDTO.builder()
        .amount(amount)
        .categoryId(categoryId)
        .type(type)
        .build();

    when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(savedCategory));    
    when(transactionRepository.save(any(Transaction.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);

    // Act
    TransactionDTO result = transactionService.save(dto);

    // Assert
    verify(transactionRepository).save(captor.capture());
    Transaction captured = captor.getValue();

    assertEquals(now, captured.getDate());
    assertNull(captured.getDescription());

    assertEquals(amount, result.getAmount());
    assertEquals(type, result.getType());
    assertEquals(now, result.getDate());
    assertNull(result.getDescription());

    CategoryDTO categoryDTO = result.getCategory();
    assertEquals(categoryId, categoryDTO.getId());
    assertEquals("Groceries", categoryDTO.getName());
    assertEquals(type, categoryDTO.getType());
  }

}
