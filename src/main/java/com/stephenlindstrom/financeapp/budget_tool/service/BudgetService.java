package com.stephenlindstrom.financeapp.budget_tool.service;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import com.stephenlindstrom.financeapp.budget_tool.dto.BudgetCreateDTO;
import com.stephenlindstrom.financeapp.budget_tool.dto.BudgetDTO;

public interface BudgetService {
  BudgetDTO create(BudgetCreateDTO dto);
  List<BudgetDTO> getAll();
  Optional<BudgetDTO> getById(Long id);
  void deleteById(Long id);
  boolean existsByCategoryIdAndMonth(Long categoryId, YearMonth month);
}
