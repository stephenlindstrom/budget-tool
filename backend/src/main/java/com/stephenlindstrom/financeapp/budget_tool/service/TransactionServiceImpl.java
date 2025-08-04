package com.stephenlindstrom.financeapp.budget_tool.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.stephenlindstrom.financeapp.budget_tool.dto.CategoryDTO;
import com.stephenlindstrom.financeapp.budget_tool.dto.TransactionCreateDTO;
import com.stephenlindstrom.financeapp.budget_tool.dto.TransactionDTO;
import com.stephenlindstrom.financeapp.budget_tool.dto.TransactionFilter;
import com.stephenlindstrom.financeapp.budget_tool.errors.ResourceNotFoundException;
import com.stephenlindstrom.financeapp.budget_tool.model.Category;
import com.stephenlindstrom.financeapp.budget_tool.model.Transaction;
import com.stephenlindstrom.financeapp.budget_tool.model.User;
import com.stephenlindstrom.financeapp.budget_tool.repository.CategoryRepository;
import com.stephenlindstrom.financeapp.budget_tool.repository.TransactionRepository;

/**
 * Service implementation for managing transactions.
 * Supports creation, retrieval, filtering, updating, and deletion of transactions.
 */
@Service
public class TransactionServiceImpl implements TransactionService {

  private final TransactionRepository transactionRepository;
  private final CategoryRepository categoryRepository;
  private final UserService userService;

  public TransactionServiceImpl(TransactionRepository transactionRepository, CategoryRepository categoryRepository, UserService userService) {
    this.transactionRepository = transactionRepository;
    this.categoryRepository = categoryRepository;
    this.userService = userService;
  }

  /**
   * Creates a new transaction.
   *
   * @param dto the data for the new transaction
   * @return the created TransactionDTO
   */
  @Override
  public TransactionDTO create(TransactionCreateDTO dto) {
    User user = userService.getAuthenticatedUser();
    Transaction transaction = mapToEntity(dto, user);
    Transaction saved = transactionRepository.save(transaction);
    return mapToDTO(saved);
  }

  /**
   * Retrieves all transactions, sorted in descending order by date.
   *
   * @return list of all TransactionDTOs
   */
  @Override
  public List<TransactionDTO> getAll() {
    User user = userService.getAuthenticatedUser();
    return transactionRepository.findByUserOrderByDateDesc(user)
            .stream()
            .map(this::mapToDTO)
            .toList();
  }

  /**
   * Filters transactions based on type, category, and date range.
   *
   * @param filter the filter criteria
   * @return list of TransactionDTOs matching the filter, sorted by date descending
   */
  @Override
  public List<TransactionDTO> filter(TransactionFilter filter) {
    User user = userService.getAuthenticatedUser();
    List<Transaction> result = transactionRepository.findByUserOrderByDateDesc(user);

    if (filter.getType() != null) {
      result = result.stream()
              .filter(t -> t.getType() == filter.getType())
              .toList();
    }

    if (filter.getCategoryId() != null) {
      result = result.stream()
              .filter(t -> t.getCategory() != null && t.getCategory().getId().equals(filter.getCategoryId()))
              .toList();
    }

    if (filter.getStartDate() != null) {
      result = result.stream()
              .filter(t -> !t.getDate().isBefore(filter.getStartDate()))
              .toList();
    }

    if (filter.getEndDate() != null) {
      result = result.stream()
              .filter(t -> !t.getDate().isAfter(filter.getEndDate()))
              .toList();
    }

    return result.stream()
            .map(this::mapToDTO)
            .toList();
  }

  /**
   * Updates an existing transaction by ID.
   *
   * @param id the ID of the transaction to update
   * @param dto the new transaction data
   * @return the updated TransactionDTO
   * @throws ResourceNotFoundException if the transaction or category is not found
   */
  @Override
  public TransactionDTO updateById(Long id, TransactionCreateDTO dto) {
    User user = userService.getAuthenticatedUser();

    Transaction transaction = transactionRepository.findByIdAndUser(id, user)
      .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

    Category category = categoryRepository.findByIdAndUser(dto.getCategoryId(), user)
      .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

    transaction.setAmount(dto.getAmount());
    transaction.setCategory(category);
    transaction.setType(dto.getType());
    transaction.setDate(dto.getDate());
    transaction.setDescription(dto.getDescription());

    Transaction updatedTransaction = transactionRepository.save(transaction);
    return mapToDTO(updatedTransaction);
  }

  /**
   * Deletes a transaction by its ID.
   *
   * @param id the ID of the transaction to delete
   */
  @Override
  @Transactional
  public void deleteById(Long id) {
    User user = userService.getAuthenticatedUser();
    transactionRepository.deleteByIdAndUser(id, user);
  }

  /**
   * Converts a Transaction entity to a TransactionDTO.
   *
   * @param transaction the Transaction entity
   * @return the mapped TransactionDTO
   */
  private TransactionDTO mapToDTO(Transaction transaction) {
    Category category = transaction.getCategory();

    CategoryDTO categoryDTO = CategoryDTO.builder()
                              .id(category.getId())
                              .name(category.getName())
                              .type(category.getType())
                              .build();

    return TransactionDTO.builder()
            .id(transaction.getId())
            .amount(transaction.getAmount())
            .category(categoryDTO)
            .type(transaction.getType())
            .date(transaction.getDate())
            .description(transaction.getDescription())
            .build();
  }

  /**
   * Converts a TransactionCreateDTO to a Transaction entity.
   * Defaults to the current date if none is provided.
   *
   * @param dto the input data
   * @return the mapped Transaction entity
   * @throws ResourceNotFoundException if the category is not found
   */
  private Transaction mapToEntity(TransactionCreateDTO dto, User user) {
    Category category = categoryRepository.findByIdAndUser(dto.getCategoryId(), user)
      .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

    return Transaction.builder()
            .amount(dto.getAmount())
            .category(category)
            .type(dto.getType())
            .date(dto.getDate() != null ? dto.getDate() : LocalDate.now()) // fallback to today if null
            .description(dto.getDescription())
            .user(user)
            .build();
  }
}
