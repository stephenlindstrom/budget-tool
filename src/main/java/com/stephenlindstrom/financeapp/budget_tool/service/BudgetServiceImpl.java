package com.stephenlindstrom.financeapp.budget_tool.service;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import com.stephenlindstrom.financeapp.budget_tool.dto.BudgetCreateDTO;
import com.stephenlindstrom.financeapp.budget_tool.dto.BudgetDTO;
import com.stephenlindstrom.financeapp.budget_tool.dto.CategoryDTO;
import com.stephenlindstrom.financeapp.budget_tool.model.Budget;
import com.stephenlindstrom.financeapp.budget_tool.model.Category;
import com.stephenlindstrom.financeapp.budget_tool.repository.BudgetRepository;
import com.stephenlindstrom.financeapp.budget_tool.repository.CategoryRepository;

public class BudgetServiceImpl implements BudgetService {

  private final BudgetRepository budgetRepository;
  private final CategoryRepository categoryRepository;

  public BudgetServiceImpl(BudgetRepository budgetRepository, CategoryRepository categoryRepository) {
    this.budgetRepository = budgetRepository;
    this.categoryRepository = categoryRepository;
  }

  @Override
  public BudgetDTO create(BudgetCreateDTO dto) {
    Budget budget = mapToEntity(dto);
    Budget saved = budgetRepository.save(budget);
    return mapToDTO(saved);
  }

  @Override
  public List<BudgetDTO> getAll() {
    return budgetRepository.findAll().stream()
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
  

  private Budget mapToEntity(BudgetCreateDTO dto) {
    Category category = categoryRepository.findById(dto.getCategoryId())
      .orElseThrow(() -> new RuntimeException("Category not found"));

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

}
