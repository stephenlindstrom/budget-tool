package com.stephenlindstrom.financeapp.budget_tool.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.stephenlindstrom.financeapp.budget_tool.dto.CategoryCreateDTO;
import com.stephenlindstrom.financeapp.budget_tool.dto.CategoryDTO;
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
  public CategoryDTO create(CategoryCreateDTO dto) {
    Category category = mapToEntity(dto);
    Category saved = categoryRepository.save(category);
    return mapToDTO(saved);
  }

  @Override
  public List<CategoryDTO> getAll() {
    return categoryRepository.findAll(Sort.by("name")).stream()   
            .map(this::mapToDTO)
            .toList();
  }

  @Override
  public Optional<CategoryDTO> getById(Long id) {
    return categoryRepository.findById(id)
            .map(this::mapToDTO);
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
  public List<CategoryDTO> getByType(TransactionType type) {
    return categoryRepository.findAll().stream() 
            .filter(c -> c.getType() == type)
            .map(this::mapToDTO)
            .toList();
  }

  private Category mapToEntity(CategoryCreateDTO dto) {
    return Category.builder()
            .name(dto.getName())
            .type(dto.getType())
            .build();
  }

  private CategoryDTO mapToDTO(Category category) {
    return CategoryDTO.builder()
            .id(category.getId())
            .name(category.getName())
            .type(category.getType())
            .build();
  }

}
