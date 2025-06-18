package com.stephenlindstrom.financeapp.budget_tool.service;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

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

@Service
public class BudgetServiceImpl implements BudgetService {

  private final BudgetRepository budgetRepository;
  private final CategoryRepository categoryRepository;
  private final TransactionService transactionService;

  public BudgetServiceImpl(BudgetRepository budgetRepository, CategoryRepository categoryRepository, TransactionService transactionService) {
    this.budgetRepository = budgetRepository;
    this.categoryRepository = categoryRepository;
    this.transactionService = transactionService;
  }

  @Override
  public BudgetDTO create(BudgetCreateDTO dto) {
    Budget budget = mapToEntity(dto);
    Budget saved = budgetRepository.save(budget);
    return mapToDTO(saved);
  }

  @Override
  public List<BudgetDTO> getAll() {
    return budgetRepository.findAll(Sort.by(Sort.Direction.DESC, "month")).stream()
      .map(this::mapToDTO)
      .toList();
  }

  @Override
  public Optional<BudgetDTO> getById(Long id) {
    return budgetRepository.findById(id).map(this::mapToDTO);
  }

  @Override
  public void deleteById(Long id) {
    budgetRepository.deleteById(id);
  }

  @Override
  public boolean existsByCategoryIdAndMonth(Long categoryId, YearMonth month) {
    return budgetRepository.existsByCategoryIdAndMonth(categoryId, month);
  }

  @Override
  public BudgetSummaryDTO getBudgetSummary(Long id) {
    Budget budget = budgetRepository.findById(id)
      .orElseThrow(() -> new ResourceNotFoundException("Budget not found"));
    
    TransactionFilter filter = TransactionFilter.builder()
                              .type(TransactionType.EXPENSE)
                              .categoryId(budget.getCategory().getId())
                              .startDate(budget.getMonth().atDay(1))
                              .endDate(budget.getMonth().atEndOfMonth())
                              .build();

    List<TransactionDTO> expenseTransactions = transactionService.filter(filter);

    BigDecimal spent = expenseTransactions.stream().map(TransactionDTO::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal budgeted = budget.getValue();

    BigDecimal remaining = budgeted.subtract(spent);

    BudgetSummaryDTO budgetSummary = BudgetSummaryDTO.builder()
                                      .budgeted(budgeted)
                                      .spent(spent)
                                      .remaining(remaining)
                                      .build();

    return budgetSummary;
    
  }

  @Override
  public List<BudgetDTO> getByMonth(YearMonth month) {
    return budgetRepository.findByMonth(month).stream().map(this::mapToDTO).toList();
  }

  @Override
  public List<MonthDTO> getAvailableMonths() {
    return budgetRepository.findDistinctMonths().stream().map(this::mapToDTO).toList();
  }
  

  private Budget mapToEntity(BudgetCreateDTO dto) {
    Category category = categoryRepository.findById(dto.getCategoryId())
      .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

    return Budget.builder()
            .value(dto.getValue())
            .month(dto.getMonth())
            .category(category)
            .build();
  }

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

  private MonthDTO mapToDTO(YearMonth month) {
    DateTimeFormatter valueFormatter = DateTimeFormatter.ofPattern("yyyy-MM");
    DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("MMMM yyyy");
    return MonthDTO.builder()
            .value(month.format(valueFormatter))
            .display(month.format(displayFormatter))
            .build();
  }

}
