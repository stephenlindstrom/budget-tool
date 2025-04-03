package com.stephenlindstrom.financeapp.budget_tool.service;

import java.util.List;
import java.util.Optional;

import com.stephenlindstrom.financeapp.budget_tool.dto.CategoryCreateDTO;
import com.stephenlindstrom.financeapp.budget_tool.dto.CategoryDTO;
import com.stephenlindstrom.financeapp.budget_tool.enums.TransactionType;

public interface CategoryService {
  CategoryDTO create(CategoryCreateDTO dto);
  List<CategoryDTO> getAll();
  Optional<CategoryDTO> getById(Long id);
  void deleteById(Long id);
  boolean existsByNameIgnoreCase(String name);
  List<CategoryDTO> getByType(TransactionType type);
}
