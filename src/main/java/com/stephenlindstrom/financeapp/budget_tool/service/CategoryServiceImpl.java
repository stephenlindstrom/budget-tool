package com.stephenlindstrom.financeapp.budget_tool.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.stephenlindstrom.financeapp.budget_tool.enums.TransactionType;
import com.stephenlindstrom.financeapp.budget_tool.model.Category;
import com.stephenlindstrom.financeapp.budget_tool.repository.CategoryRepository;

@Service
public class CategoryServiceImpl implements CategoryService {

  private final CategoryRepository categoryRepository;

  public CategoryServiceImpl(CategoryRepository categoryRepository) {
    this.categoryRepository = categoryRepository;
  }

  @Override
  public Category create(Category category) {
    return categoryRepository.save(category);
  }

  @Override
  public List<Category> getAll() {
    return categoryRepository.findAll();
  }

  @Override
  public Optional<Category> getById(Long id) {
    return categoryRepository.findById(id);
  }

  @Override
  public void deleteById(Long id) {
    categoryRepository.deleteById(id);
  }

  @Override
  public boolean existsByNameIgnoreCase(String name) {
    return categoryRepository.existsByNameIgnoreCase(name);
  }

  @Override
  public List<Category> getByType(TransactionType type) {
    List<Category> result = categoryRepository.findAll();
    result = result.stream() 
            .filter(c -> c.getType() == type)
            .toList();

    return result;
  }

}
