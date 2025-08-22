package com.stephenlindstrom.financeapp.budget_tool.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.stephenlindstrom.financeapp.budget_tool.dto.BudgetCreateDTO;
import com.stephenlindstrom.financeapp.budget_tool.dto.BudgetDTO;
import com.stephenlindstrom.financeapp.budget_tool.dto.BudgetSummaryDTO;
import com.stephenlindstrom.financeapp.budget_tool.dto.CategoryDTO;
import com.stephenlindstrom.financeapp.budget_tool.dto.MonthDTO;
import com.stephenlindstrom.financeapp.budget_tool.dto.MonthlyBudgetSummaryDTO;
import com.stephenlindstrom.financeapp.budget_tool.dto.MonthlyOverviewDTO;
import com.stephenlindstrom.financeapp.budget_tool.dto.TransactionDTO;
import com.stephenlindstrom.financeapp.budget_tool.dto.TransactionFilter;
import com.stephenlindstrom.financeapp.budget_tool.enums.TransactionType;
import com.stephenlindstrom.financeapp.budget_tool.errors.BadRequestException;
import com.stephenlindstrom.financeapp.budget_tool.errors.ConflictException;
import com.stephenlindstrom.financeapp.budget_tool.errors.ResourceNotFoundException;
import com.stephenlindstrom.financeapp.budget_tool.model.Budget;
import com.stephenlindstrom.financeapp.budget_tool.model.Category;
import com.stephenlindstrom.financeapp.budget_tool.model.User;
import com.stephenlindstrom.financeapp.budget_tool.repository.BudgetRepository;
import com.stephenlindstrom.financeapp.budget_tool.repository.BudgetRepository.MonthlyBudgetedRow;
import com.stephenlindstrom.financeapp.budget_tool.repository.CategoryRepository;
import com.stephenlindstrom.financeapp.budget_tool.repository.TransactionRepository;

/**
 * Service implementation for managing budget entries.
 * Handles creation, retrieval, updating, and deletion of budgets.
 * Also provides functionality for budget summaries and available months.
 */
@Service
public class BudgetServiceImpl implements BudgetService {

  private final BudgetRepository budgetRepository;
  private final CategoryRepository categoryRepository;
  private final TransactionRepository transactionRepository;
  private final TransactionService transactionService;
  private final UserService userService;

  public BudgetServiceImpl(BudgetRepository budgetRepository, CategoryRepository categoryRepository, TransactionRepository transactionRepository, TransactionService transactionService, UserService userService) {
    this.budgetRepository = budgetRepository;
    this.categoryRepository = categoryRepository;
    this.transactionRepository = transactionRepository;
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

    Category category = categoryRepository.findByIdAndUser(dto.getCategoryId(), user)
      .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

    if (category.getType() == TransactionType.INCOME) {
      throw new BadRequestException("Budgets are only allowed for expense categories.");
    }

    if (budgetRepository.existsByCategoryIdAndMonthAndUser(category.getId(), dto.getMonth(), user)) {
      throw new ConflictException("A budget already exists for this category and month.");
    }

    BigDecimal roundedValue = dto.getValue().setScale(2, RoundingMode.HALF_UP);
    
    Budget budget = mapToEntity(dto, user, category);
    budget.setValue(roundedValue);
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

     if (category.getType() == TransactionType.INCOME) {
      throw new BadRequestException("Budgets are only allowed for expense categories.");
    }

    if (budgetRepository.existsByCategoryIdAndMonthAndUserAndIdNot(category.getId(), dto.getMonth(), user, id)) {
      throw new ConflictException("A budget already exists for this category and month.");
    }

    BigDecimal roundedValue = dto.getValue().setScale(2, RoundingMode.HALF_UP);
      
    budget.setValue(roundedValue);
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
   * @return a MonthlyBudgetSummaryDTO containing the month metadata and a list of summaries
   */

  @Override
public MonthlyBudgetSummaryDTO getMonthlyBudgetSummaries(YearMonth month) {
  User user = userService.getAuthenticatedUser();

  final LocalDate start = month.atDay(1);
  final LocalDate endExclusive = month.plusMonths(1).atDay(1);

  // 1) Budgets for the month- filter to EXPENSE to avoid accidental income budgets
  final List<Budget> budgets = budgetRepository.findByMonthAndUser(month, user)
      .stream()
      .filter(b -> b.getCategory() != null
          && b.getCategory().getType() == TransactionType.EXPENSE)
      .toList();

  // Map budgets by categoryId for quick lookup
  final Map<Long, Budget> budgetByCategory = budgets.stream()
      .collect(Collectors.toMap(b -> b.getCategory().getId(), b -> b));

  // 2) Spent by category for the month
  final Map<Long, BigDecimal> spentByCategory = transactionRepository
      .findMonthlySpentByCategory(user, start, endExclusive, TransactionType.EXPENSE)
      .stream()
      .collect(Collectors.toMap(
          TransactionRepository.SpentByCategoryRow::getCategoryId,
          row -> nvl(row.getSpent())
      ));

  // 3) Union of category IDs from budgets + transactions
  final Set<Long> categoryIds = new HashSet<>(budgetByCategory.keySet());
  categoryIds.addAll(spentByCategory.keySet());

  // 4) Fetch any missing Category entities in one batch
  final Set<Long> missingIds = categoryIds.stream()
      .filter(id -> !budgetByCategory.containsKey(id))
      .collect(Collectors.toSet());

  final Map<Long, Category> extraCategories = missingIds.isEmpty()
      ? Collections.emptyMap()
      : categoryRepository.findAllById(missingIds).stream()
          .collect(Collectors.toMap(Category::getId, c -> c));

  // 5) Build DTOs for every category in the union
  final List<BudgetSummaryDTO> budgetSummaries = categoryIds.stream()
      .map(catId -> {
        final Budget budget = budgetByCategory.get(catId);
        final Category category = (budget != null)
            ? budget.getCategory()
            : extraCategories.get(catId);

        // If category was deleted but transactions remain, guard with a placeholder
        final Long categoryId = (category != null) ? category.getId() : catId;
        final String categoryName = (category != null) ? category.getName() : "(Unknown Category)";
        final TransactionType categoryType = (category != null) ? category.getType() : TransactionType.EXPENSE;

        
        if (categoryType != TransactionType.EXPENSE) return null;

        final BigDecimal budgeted = nvl(budget != null ? budget.getValue() : null); // 0 if no budget
        final BigDecimal spent = nvl(spentByCategory.get(catId));
        final BigDecimal remaining = budgeted.subtract(spent);

        final CategoryDTO categoryDTO = CategoryDTO.builder()
            .id(categoryId)
            .name(categoryName)
            .type(categoryType)
            .build();

        return BudgetSummaryDTO.builder()
            .id(budget != null ? budget.getId() : null) // null => “no budget yet”
            .category(categoryDTO)
            .budgeted(budgeted)
            .spent(spent)
            .remaining(remaining)
            .build();
      })
      .filter(Objects::nonNull)
      // nice default sort: by category name
      .sorted(Comparator.comparing(d -> d.getCategory().getName(), String.CASE_INSENSITIVE_ORDER))
      .toList();

  return MonthlyBudgetSummaryDTO.builder()
      .monthDTO(mapToDTO(month))
      .budgetSummaryDTOs(budgetSummaries)
      .build();
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

  @Override
  public List<MonthlyOverviewDTO> getMonthlyOverviews() {
    User user = userService.getAuthenticatedUser();

    // budgeted per month (EXPENSE categories only)
    Map<YearMonth, BigDecimal> budgetedByMonth =
        budgetRepository.findMonthlyBudgeted(user).stream()
            .collect(Collectors.toMap(
                MonthlyBudgetedRow::getMonth,
                r -> nvl(r.getBudgeted())
            ));

    // spent per month (expenses)
    Map<YearMonth, BigDecimal> spentByMonth =
        transactionRepository.findMonthlySpentByMonth(user, TransactionType.EXPENSE).stream()
            .collect(Collectors.toMap(
                r -> YearMonth.of(r.getYear(), r.getMonth()),
                r -> nvl(r.getSpent())
            ));

    // income per month
    Map<YearMonth, BigDecimal> incomeByMonth =
        transactionRepository.findMonthlySpentByMonth(user, TransactionType.INCOME).stream()
            .collect(Collectors.toMap(
                r -> YearMonth.of(r.getYear(), r.getMonth()),
                r -> nvl(r.getSpent()) // alias is "spent" in interface; value will be income here
            ));

    // union of all months, newest first
    NavigableSet<YearMonth> months = new TreeSet<>(Comparator.reverseOrder());
    months.addAll(budgetedByMonth.keySet());
    months.addAll(spentByMonth.keySet());
    months.addAll(incomeByMonth.keySet());

    return months.stream()
        .map(m -> {
          BigDecimal budgeted = budgetedByMonth.getOrDefault(m, BigDecimal.ZERO);
          BigDecimal spent = spentByMonth.getOrDefault(m, BigDecimal.ZERO);
          BigDecimal income = incomeByMonth.getOrDefault(m, BigDecimal.ZERO);
          BigDecimal remaining = budgeted.subtract(spent);
          return MonthlyOverviewDTO.builder()
              .month(m)
              .budgeted(budgeted)
              .spent(spent)
              .remaining(remaining)
              .income(income)
              .build();
        })
        .collect(Collectors.toList());
  }

  @Override
  public MonthlyOverviewDTO getMonthlyOverviewByMonth(YearMonth month) {
    return getMonthlyOverviews().stream()
        .filter(dto -> dto.getMonth().equals(month))
        .findFirst()
        .orElseGet(() -> MonthlyOverviewDTO.builder()
            .month(month)
            .budgeted(BigDecimal.ZERO)
            .spent(BigDecimal.ZERO)
            .remaining(BigDecimal.ZERO)
            .income(BigDecimal.ZERO)
            .build());
  }

  /**
   * Maps a BudgetCreateDTO to a Budget entity.
   * 
   * @param dto the input DTO
   * @return the Budget entity
   * @throws ResourceNotFoundException if the category is not found
   */
  private Budget mapToEntity(BudgetCreateDTO dto, User user, Category category) {
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

  private static BigDecimal nvl(BigDecimal v) {
    return v == null ? BigDecimal.ZERO : v;
  }
}
