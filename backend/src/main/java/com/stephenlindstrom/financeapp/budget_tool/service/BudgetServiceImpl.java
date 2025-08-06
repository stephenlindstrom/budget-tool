package com.stephenlindstrom.financeapp.budget_tool.service;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
import com.stephenlindstrom.financeapp.budget_tool.model.User;
import com.stephenlindstrom.financeapp.budget_tool.repository.BudgetRepository;
import com.stephenlindstrom.financeapp.budget_tool.repository.CategoryRepository;

/**
 * Service implementation for managing budget entries.
 * Handles creation, retrieval, updating, and deletion of budgets.
 * Also provides functionality for budget summaries and available months.
 */
@Service
public class BudgetServiceImpl implements BudgetService {

  private final BudgetRepository budgetRepository;
  private final CategoryRepository categoryRepository;
  private final TransactionService transactionService;
  private final UserService userService;

  public BudgetServiceImpl(BudgetRepository budgetRepository, CategoryRepository categoryRepository, TransactionService transactionService, UserService userService) {
    this.budgetRepository = budgetRepository;
    this.categoryRepository = categoryRepository;
    this.transactionService = transactionService;
    this.userService = userService;
  }

  /**
   * Creates a new budget entry.
   * 
   * @param dto the budget data to save
   * @return the saved BudgetDTO
   */
  @Override
  public BudgetDTO create(BudgetCreateDTO dto) {
    User user = userService.getAuthenticatedUser();
    
    Budget budget = mapToEntity(dto, user);
    Budget saved = budgetRepository.save(budget);
    return mapToDTO(saved);
  }

  /**
   * Retrieves all budgets, sorted by month in descending order.
   * 
   * @return list of all BudgetDTOs
   */
  @Override
  public List<BudgetDTO> getAll() {
    User user = userService.getAuthenticatedUser();
      
    return budgetRepository.findByUserOrderByMonthDesc(user).stream()
      .map(this::mapToDTO)
      .toList();
  }

  /**
   * Retrieves a budget by its ID.
   * 
   * @param id the budget ID
   * @return Optional containing the BudgetDTO if found
   */
  @Override
  public Optional<BudgetDTO> getById(Long id) {
    User user = userService.getAuthenticatedUser();
    return budgetRepository.findByIdAndUser(id, user).map(this::mapToDTO);
  }

  /**
   * Updates an existing budget by ID.
   * 
   * @param id the ID of the budget to update
   * @param dto the new budget data
   * @return the updated BudgetDTO
   * @throws ResourceNotFoundException if the budget or category is not found
   */
  @Override
  public BudgetDTO updateById(Long id, BudgetCreateDTO dto) {
    User user = userService.getAuthenticatedUser();

    Budget budget = budgetRepository.findByIdAndUser(id, user)
        .orElseThrow(() -> new ResourceNotFoundException("Budget not found"));
      
    Category category = categoryRepository.findByIdAndUser(dto.getCategoryId(), user)
        .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
      
    budget.setValue(dto.getValue());
    budget.setMonth(dto.getMonth());
    budget.setCategory(category);
    
    Budget updatedBudget = budgetRepository.save(budget);

    return mapToDTO(updatedBudget);
  }

  /**
   * Deletes a budget by its ID.
   * 
   * @param id the ID of the budget to delete
   */
  @Override
  @Transactional
  public void deleteById(Long id) {
    User user = userService.getAuthenticatedUser();
    budgetRepository.deleteByIdAndUser(id, user);
  }

  /**
   * Checks if a budget exists for a specific category and month.
   * 
   * @param categoryId the category ID
   * @param month the year-month combination to check
   * @return true if a budget exists, false otherwise
   */
  @Override
  public boolean existsByCategoryIdAndMonth(Long categoryId, YearMonth month) {
    User user = userService.getAuthenticatedUser();
    return budgetRepository.existsByCategoryIdAndMonthAndUser(categoryId, month, user);
  }

  /**
   * Generates a summary of the budget including amount spend and remaining.
   * 
   * @param id the budget ID
   * @return the budget summary
   * @throws ResourceNotFoundException if the budget is not found
   */
  @Override
  public BudgetSummaryDTO getBudgetSummary(Long id) {
    User user = userService.getAuthenticatedUser();

    Budget budget = budgetRepository.findByIdAndUser(id, user)
      .orElseThrow(() -> new ResourceNotFoundException("Budget not found"));
    
    // Build a transaction filter to get all expenses for the budget's category and month
    TransactionFilter filter = TransactionFilter.builder()
                              .type(TransactionType.EXPENSE)
                              .categoryId(budget.getCategory().getId())
                              .startDate(budget.getMonth().atDay(1))
                              .endDate(budget.getMonth().atEndOfMonth())
                              .build();

    BigDecimal spent = transactionService.filter(filter).stream()
    .map(TransactionDTO::getAmount)
    .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal budgeted = budget.getValue();

    BigDecimal remaining = budgeted.subtract(spent);

    CategoryDTO categoryDTO = CategoryDTO.builder()
                                .id(budget.getCategory().getId())
                                .name(budget.getCategory().getName())
                                .type(budget.getCategory().getType())
                                .build();

    BudgetSummaryDTO budgetSummary = BudgetSummaryDTO.builder()
                                      .id(budget.getId())
                                      .category(categoryDTO)
                                      .budgeted(budgeted)
                                      .spent(spent)
                                      .remaining(remaining)
                                      .build();

    return budgetSummary;
    
  }

  /**
   * Generates budget summaries for all budgets in a given month.
   * 
   * @param month the year-month to filter budgets by
   * @return a list of summary DTOs for each budget
   */

  @Override
  public List<BudgetSummaryDTO> getMonthlyBudgetSummaries(YearMonth month) {
    User user = userService.getAuthenticatedUser();
    List<Budget> budgets = budgetRepository.findByMonthAndUser(month, user);

    return budgets.stream().map(budget -> {
      BigDecimal budgeted = budget.getValue();

      TransactionFilter filter = TransactionFilter.builder()
                                .type(TransactionType.EXPENSE)
                                .categoryId(budget.getCategory().getId())
                                .startDate(budget.getMonth().atDay(1))
                                .endDate(budget.getMonth().atEndOfMonth())
                                .build();

      BigDecimal spent = transactionService.filter(filter).stream()
        .map(TransactionDTO::getAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

      BigDecimal remaining = budgeted.subtract(spent);

      CategoryDTO categoryDTO = CategoryDTO.builder()
                                .id(budget.getCategory().getId())
                                .name(budget.getCategory().getName())
                                .type(budget.getCategory().getType())
                                .build();

      return BudgetSummaryDTO.builder()
                            .id(budget.getId())
                            .category(categoryDTO)
                            .budgeted(budgeted)
                            .spent(spent)
                            .remaining(remaining)
                            .build();
    }).toList();
  }

  /**
   * Retrieves all budgets for a specific month.
   * 
   * @param month the month to filter by
   * @return list of BudgetDTOs for the given month
   */
  @Override
  public List<BudgetDTO> getByMonth(YearMonth month) {
    User user = userService.getAuthenticatedUser();
    return budgetRepository.findByMonthAndUser(month, user).stream().map(this::mapToDTO).toList();
  }

  /**
   * Retrieves a list of months for which budgets exist.
   * Sorted in reverse chronological order.
   * 
   * @return list of MonthDTOs
   */
  @Override
  public List<MonthDTO> getAvailableMonths() {
    User user = userService.getAuthenticatedUser();
    return budgetRepository.findDistinctMonthsByUser(user).stream().sorted(Comparator.reverseOrder()).map(this::mapToDTO).toList();
  }
  
  /**
   * Maps a BudgetCreateDTO to a Budget entity.
   * 
   * @param dto the input DTO
   * @return the Budget entity
   * @throws ResourceNotFoundException if the category is not found
   */
  private Budget mapToEntity(BudgetCreateDTO dto, User user) {
    Category category = categoryRepository.findByIdAndUser(dto.getCategoryId(), user)
      .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

    return Budget.builder()
            .value(dto.getValue())
            .month(dto.getMonth())
            .category(category)
            .user(user)
            .build();
  }

  /**
   * Maps a Budget entity to a BudgetDTO.
   * 
   * @param budget the Budget entity
   * @return the BudgetDTO
   */
  private BudgetDTO mapToDTO(Budget budget) {
    Category category = budget.getCategory();

    CategoryDTO categoryDTO = CategoryDTO.builder()
                                .id(category.getId())
                                .name(category.getName())
                                .type(category.getType())
                                .build();

    return BudgetDTO.builder()
            .id(budget.getId())
            .value(budget.getValue())
            .month(budget.getMonth())
            .category(categoryDTO)
            .build();
  }

  /**
   * Maps a YearMonth to a MonthDTO with formatted value and display strings.
   * 
   * @param month the month to format
   * @return the MonthDTO
   */
  private MonthDTO mapToDTO(YearMonth month) {
    DateTimeFormatter valueFormatter = DateTimeFormatter.ofPattern("yyyy-MM");
    DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("MMMM yyyy");
    return MonthDTO.builder()
            .value(month.format(valueFormatter))
            .display(month.format(displayFormatter))
            .build();
  }
}
