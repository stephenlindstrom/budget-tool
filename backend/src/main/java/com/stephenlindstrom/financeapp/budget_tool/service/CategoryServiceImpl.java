package com.stephenlindstrom.financeapp.budget_tool.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.stephenlindstrom.financeapp.budget_tool.dto.CategoryCreateDTO;
import com.stephenlindstrom.financeapp.budget_tool.dto.CategoryDTO;
import com.stephenlindstrom.financeapp.budget_tool.enums.TransactionType;
import com.stephenlindstrom.financeapp.budget_tool.errors.ResourceNotFoundException;
import com.stephenlindstrom.financeapp.budget_tool.model.Category;
import com.stephenlindstrom.financeapp.budget_tool.repository.CategoryRepository;

/**
 * Service implementation for managing categories.
 * Provides CRUD operations and filtering by transaction type.
 */
@Service
public class CategoryServiceImpl implements CategoryService {

  private final CategoryRepository categoryRepository;

  public CategoryServiceImpl(CategoryRepository categoryRepository) {
    this.categoryRepository = categoryRepository;
  }

  /**
   * Creates a new category.
   *
   * @param dto the data for the new category
   * @return the created CategoryDTO
   */
  @Override
  public CategoryDTO create(CategoryCreateDTO dto) {
    Category category = mapToEntity(dto);
    Category saved = categoryRepository.save(category);
    return mapToDTO(saved);
  }

  /**
   * Retrieves all categories, sorted by name.
   *
   * @return list of all CategoryDTOs
   */
  @Override
  public List<CategoryDTO> getAll() {
    return categoryRepository.findAll(Sort.by("name")).stream()
            .map(this::mapToDTO)
            .toList();
  }

  /**
   * Retrieves a category by its ID.
   *
   * @param id the category ID
   * @return an Optional containing the CategoryDTO if found
   */
  @Override
  public Optional<CategoryDTO> getById(Long id) {
    return categoryRepository.findById(id)
            .map(this::mapToDTO);
  }

  /**
   * Updates an existing category by ID.
   *
   * @param id the ID of the category to update
   * @param dto the new category data
   * @return the updated CategoryDTO
   * @throws ResourceNotFoundException if the category is not found
   */
  @Override
  public CategoryDTO updateById(Long id, CategoryCreateDTO dto) {
    Category category = categoryRepository.findById(id)
      .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

    category.setName(dto.getName());
    category.setType(dto.getType());

    Category updatedCategory = categoryRepository.save(category);
    return mapToDTO(updatedCategory);
  }

  /**
   * Deletes a category by its ID.
   *
   * @param id the ID of the category to delete
   */
  @Override
  public void deleteById(Long id) {
    categoryRepository.deleteById(id);
  }

  /**
   * Checks if a category exists by name (case-insensitive).
   *
   * @param name the category name
   * @return true if a category exists, false otherwise
   */
  @Override
  public boolean existsByNameIgnoreCase(String name) {
    return categoryRepository.existsByNameIgnoreCase(name);
  }

  /**
   * Retrieves all categories of a specific transaction type (INCOME or EXPENSE).
   *
   * @param type the transaction type to filter by
   * @return list of CategoryDTOs matching the type
   */
  @Override
  public List<CategoryDTO> getByType(TransactionType type) {
    return categoryRepository.findAll().stream()
            .filter(c -> c.getType() == type)
            .map(this::mapToDTO)
            .toList();
  }

  /**
   * Maps a CategoryCreateDTO to a Category entity.
   *
   * @param dto the input data
   * @return the Category entity
   */
  private Category mapToEntity(CategoryCreateDTO dto) {
    return Category.builder()
            .name(dto.getName())
            .type(dto.getType())
            .build();
  }

  /**
   * Maps a Category entity to a CategoryDTO.
   *
   * @param category the entity
   * @return the DTO
   */
  private CategoryDTO mapToDTO(Category category) {
    return CategoryDTO.builder()
            .id(category.getId())
            .name(category.getName())
            .type(category.getType())
            .build();
  }
}