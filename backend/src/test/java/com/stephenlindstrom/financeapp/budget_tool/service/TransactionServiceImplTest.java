package com.stephenlindstrom.financeapp.budget_tool.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import com.stephenlindstrom.financeapp.budget_tool.dto.CategoryDTO;
import com.stephenlindstrom.financeapp.budget_tool.dto.TransactionCreateDTO;
import com.stephenlindstrom.financeapp.budget_tool.dto.TransactionDTO;
import com.stephenlindstrom.financeapp.budget_tool.dto.TransactionFilter;
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

  private static List<Transaction> transactionBatch;

  @BeforeAll
  static void initData() {
    Category category1 = Category.builder()
                          .id(1L)
                          .name("Groceries")
                          .type(TransactionType.EXPENSE)
                          .build();
    
    Category category2 = Category.builder()
                          .id(2L)
                          .name("Car")
                          .type(TransactionType.EXPENSE)
                          .build();

    Category category3 = Category.builder()
                          .id(3L)
                          .name("Salary")
                          .type(TransactionType.INCOME)
                          .build();

    Transaction transaction1 = Transaction.builder()
                                .id(1L)
                                .amount(BigDecimal.valueOf(100.00))
                                .category(category1)
                                .type(category1.getType())
                                .date(LocalDate.of(2025, 5, 1))
                                .description("food")
                                .build();

    Transaction transaction2 = Transaction.builder()
                                .id(2L)
                                .amount(BigDecimal.valueOf(50.00))
                                .category(category2)
                                .type(category2.getType())
                                .date(LocalDate.of(2025, 6, 4))
                                .description("gas")
                                .build();
    
    Transaction transaction3 = Transaction.builder()
                                .id(3L)
                                .amount(BigDecimal.valueOf(150.00))
                                .category(category2)
                                .type(category2.getType())
                                .date(LocalDate.of(2024, 12, 23))
                                .description("repairs")
                                .build();

    Transaction transaction4 = Transaction.builder()
                                .id(4L)
                                .amount(BigDecimal.valueOf(2000.00))
                                .category(category3)
                                .type(category3.getType())
                                .date(LocalDate.of(2025, 4, 25))
                                .description("paycheck")
                                .build();

    transactionBatch = Arrays.asList(transaction1, transaction2, transaction3, transaction4);
  }

  @Test
  void testCreate_WithValidInput_ReturnsTransactionDTO() {
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
    TransactionDTO result = transactionService.create(dto);

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
  void testCreate_WithInvalidCategoryId_ThrowsResourceNotFoundException() {
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
      transactionService.create(dto);
    });

    assertEquals("Category not found", exception.getMessage());
    verify(transactionRepository, never()).save(any(Transaction.class));
  }

  @Test
  void testCreate_WithNullDateAndNullDescription_ReturnsTransactionDTOWithCurrentDateAndEmptyDescription() {
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
    TransactionDTO result = transactionService.create(dto);

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

  @Test
  void testGetAll_WithEntries_ReturnsListofTransactionDTO() {
    // Arrange
    TransactionType type = TransactionType.EXPENSE;

    BigDecimal amount1 = BigDecimal.valueOf(100.00);
    LocalDate date1 = LocalDate.of(2025, 6, 5);
    String description1 = "food";

    BigDecimal amount2 = BigDecimal.valueOf(50.00);
    LocalDate date2 = LocalDate.of(2025, 5, 1);
    String description2 = "gas";


    Category savedCategory1 = Category.builder()
        .id(1L)
        .name("Groceries")
        .type(type)
        .build();

    Category savedCategory2 = Category.builder()
        .id(2L)
        .name("Car")
        .type(type)
        .build();
    
    Transaction savedTransaction1 = Transaction.builder()
        .id(1L)
        .amount(amount1)
        .category(savedCategory1)
        .type(type)
        .date(date1)
        .description(description1)
        .build();

    Transaction savedTransaction2 = Transaction.builder()
        .id(2L)
        .amount(amount2)
        .category(savedCategory2)
        .type(type)
        .date(date2)
        .description(description2)
        .build();

    List<Transaction> transactionList = new ArrayList<>(Arrays.asList(savedTransaction1, savedTransaction2));

    when(transactionRepository.findAll(any(Sort.class))).thenReturn(transactionList.stream().sorted(Comparator.comparing(Transaction::getDate).reversed()).toList());

    // Act
    List<TransactionDTO> dtos = transactionService.getAll();

    // Assert
    assertEquals(2, dtos.size());
    assertEquals(1L, dtos.get(0).getId());
    assertEquals(2L, dtos.get(1).getId());
    assertEquals(amount1, dtos.get(0).getAmount());
    assertEquals(amount2, dtos.get(1).getAmount());
    assertEquals(date1, dtos.get(0).getDate());
    assertEquals(date2, dtos.get(1).getDate());
    assertEquals(type, dtos.get(0).getType());
    assertEquals(type, dtos.get(1).getType());
    assertEquals(description1, dtos.get(0).getDescription());
    assertEquals(description2, dtos.get(1).getDescription());

    CategoryDTO categoryDTO1 = dtos.get(0).getCategory();
    CategoryDTO categoryDTO2 = dtos.get(1).getCategory();

    assertEquals(1L, categoryDTO1.getId());
    assertEquals(2L, categoryDTO2.getId());
    assertEquals("Groceries", categoryDTO1.getName());
    assertEquals("Car", categoryDTO2.getName());
    assertEquals(type, categoryDTO1.getType());
    assertEquals(type, categoryDTO2.getType());
  }

  @Test
  void testGetAll_WithNoEntries_ReturnsEmptyList() {
    // Arrange
    List<Transaction> transactionList = new ArrayList<>();

    when(transactionRepository.findAll(any(Sort.class))).thenReturn(transactionList.stream().sorted(Comparator.comparing(Transaction::getDate).reversed()).toList());

    // Act
    List<TransactionDTO> dtos = transactionService.getAll();

    // Assert
    assertTrue(dtos.isEmpty());
  }

  @Test
  void testUpdateById_WithExistingTransaction_ReturnsTransactionDTO() {
    // Arrange
    TransactionType type1 = TransactionType.EXPENSE;
    TransactionType type2 = TransactionType.INCOME;

    Category category1 = Category.builder()
        .id(1L)
        .name("Groceries")
        .type(type1)
        .build();

     Category category2 = Category.builder()
        .id(2L)
        .name("Salary")
        .type(type2)
        .build();
    
    Transaction existingTransaction = Transaction.builder()
        .id(1L)
        .amount(BigDecimal.valueOf(100.00))
        .category(category1)
        .type(type1)
        .date(LocalDate.of(2025, 6, 24))
        .description("Fry's")
        .build();
    
    Transaction updatedTransaction = Transaction.builder()
        .id(1L)
        .amount(BigDecimal.valueOf(1200.00))
        .category(category2)
        .type(type2)
        .date(LocalDate.of(2024, 4, 23))
        .description("Job")
        .build();

    TransactionCreateDTO dto = TransactionCreateDTO.builder()
        .amount(BigDecimal.valueOf(1200.00))
        .categoryId(category2.getId())
        .type(type2)
        .date(LocalDate.of(2024, 4, 23))
        .description("Job")
        .build();

    when(transactionRepository.findById(1L)).thenReturn(Optional.of(existingTransaction));
    when(categoryRepository.findById(dto.getCategoryId())).thenReturn(Optional.of(category2));
    when(transactionRepository.save(any(Transaction.class))).thenReturn(updatedTransaction);
    // Act
    TransactionDTO result = transactionService.updateById(1L, dto);

    // Assert
    assertEquals(1L, result.getId());
    assertEquals(BigDecimal.valueOf(1200.00), result.getAmount());
    assertEquals(2L, result.getCategory().getId());
    assertEquals(TransactionType.INCOME, result.getType());
    assertEquals(LocalDate.of(2024, 4, 23), result.getDate());
    assertEquals("Job", result.getDescription());

    verify(transactionRepository).save(any(Transaction.class));
  }

  @Test
  void testUpdateById_TransactionDoesNotExist_ThrowsResourceNotFoundException() {
    // Arrange
    TransactionCreateDTO dto = TransactionCreateDTO.builder()
        .amount(BigDecimal.valueOf(1200.00))
        .categoryId(2L)
        .type(TransactionType.INCOME)
        .date(LocalDate.of(2024, 4, 23))
        .description("Job")
        .build();

    when(transactionRepository.findById(1L)).thenReturn(Optional.empty());

    // Act and Assert
    ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
      transactionService.updateById(1L, dto);
    });
    
    assertEquals("Transaction not found", exception.getMessage());

    verifyNoInteractions(categoryRepository);
  }

  @Test
  void testUpdateById_CategoryDoesNotExist_ThrowsResourceNotFoundException() {
    // Arrange
    Category category = Category.builder()
        .id(1L)
        .name("Groceries")
        .type(TransactionType.EXPENSE)
        .build();

    Transaction existingTransaction = Transaction.builder()
        .id(1L)
        .amount(BigDecimal.valueOf(100.00))
        .category(category)
        .type(TransactionType.EXPENSE)
        .date(LocalDate.of(2025, 6, 24))
        .description("Fry's")
        .build();

    TransactionCreateDTO dto = TransactionCreateDTO.builder()
        .amount(BigDecimal.valueOf(1200.00))
        .categoryId(2L)
        .type(TransactionType.INCOME)
        .date(LocalDate.of(2024, 4, 23))
        .description("Job")
        .build();

    when(transactionRepository.findById(1L)).thenReturn(Optional.of(existingTransaction));
    when(categoryRepository.findById(2L)).thenReturn(Optional.empty());

    // Act and Assert
    ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
      transactionService.updateById(1L, dto);
    });
    
    verify(categoryRepository).findById(2L);

    assertEquals("Category not found", exception.getMessage());

    verify(transactionRepository, never()).save(any(Transaction.class));
  }

  @Test
  void testDeleteById_WithValidId_NoReturnValue() {
    // Act
    transactionService.deleteById(1L);

    // Assert
    verify(transactionRepository).deleteById(1L);
  }

  @Test
  void testFilter_NoFilterCriteria_ReturnsAllTransactions() {
    //Arrange
    when(transactionRepository.findAll()).thenReturn(transactionBatch);

    // Act
    List<TransactionDTO> dtos = transactionService.filter(new TransactionFilter()); 

    // Assert
    assertEquals(4, dtos.size());
  }

  @Test
  void testFilter_FilterByExpenseType_ReturnsAllExpenses() {
    // Arrange
    when(transactionRepository.findAll()).thenReturn(transactionBatch);
    TransactionFilter filter = new TransactionFilter(TransactionType.EXPENSE, null, null, null);

    // Act
    List<TransactionDTO> dtos = transactionService.filter(filter);

    // Assert
    assertEquals(3, dtos.size());
    assertEquals(TransactionType.EXPENSE, dtos.get(0).getType());
    assertEquals(TransactionType.EXPENSE, dtos.get(1).getType());
    assertEquals(TransactionType.EXPENSE, dtos.get(2).getType());
  }

  @Test
  void testFilter_FilterByCategoryId2_ReturnsTransactionsWithCategoryId2() {
    // Arrange
    when(transactionRepository.findAll()).thenReturn(transactionBatch);
    TransactionFilter filter = new TransactionFilter(null, 2L, null, null);

    // Act
    List<TransactionDTO> dtos = transactionService.filter(filter);

    // Assert
    assertEquals(2, dtos.size());
    assertEquals(2L, dtos.get(0).getCategory().getId());
    assertEquals(2L, dtos.get(1).getCategory().getId());
  }

  @Test
  void testFilter_FilterByDate_ReturnsTransactionsBetweenStartAndEndDates() {
    // Arrange
    when(transactionRepository.findAll()).thenReturn(transactionBatch);
    TransactionFilter filter = new TransactionFilter(null, null, LocalDate.of(2025, 4, 1), LocalDate.of(2025, 5, 1));

    // Act
    List<TransactionDTO> dtos = transactionService.filter(filter);

    // Assert
    assertEquals(2, dtos.size());
    assertTrue(!dtos.get(0).getDate().isBefore(filter.getStartDate()));
    assertTrue(!dtos.get(0).getDate().isAfter(filter.getEndDate()));
    assertTrue(!dtos.get(1).getDate().isBefore(filter.getStartDate()));
    assertTrue(!dtos.get(1).getDate().isAfter(filter.getEndDate()));
  }

  @Test
  void testFilter_FilterByAllCriteria_ReturnsCorrectlyFilteredTransactions() {
    // Arrange
    when(transactionRepository.findAll()).thenReturn(transactionBatch);
    TransactionFilter filter = new TransactionFilter(TransactionType.EXPENSE, 2L, LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31));

    // Act
    List<TransactionDTO> dtos = transactionService.filter(filter);

    // Assert
    assertEquals(1, dtos.size());
    assertEquals(2L, dtos.get(0).getId());
  }

}
