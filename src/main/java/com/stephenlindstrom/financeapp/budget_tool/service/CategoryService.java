package com.stephenlindstrom.financeapp.budget_tool.service;

import java.util.List;
import java.util.Optional;

import com.stephenlindstrom.financeapp.budget_tool.enums.TransactionType;
import com.stephenlindstrom.financeapp.budget_tool.model.Category;

public interface CategoryService {
  Category create(Category category);
  List<Category> getAll();
  Optional<Category> getById(Long id);
  void deleteById(Long id);
  boolean existsByNameIgnoreCase(String name);
  List<Category> getByType(TransactionType type);
}
