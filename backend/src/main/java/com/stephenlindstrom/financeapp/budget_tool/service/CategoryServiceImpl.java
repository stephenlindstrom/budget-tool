package com.stephenlindstrom.financeapp.budget_tool.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.stephenlindstrom.financeapp.budget_tool.dto.CategoryCreateDTO;
import com.stephenlindstrom.financeapp.budget_tool.dto.CategoryDTO;
import com.stephenlindstrom.financeapp.budget_tool.enums.TransactionType;
import com.stephenlindstrom.financeapp.budget_tool.errors.ResourceNotFoundException;
import com.stephenlindstrom.financeapp.budget_tool.model.Category;
import com.stephenlindstrom.financeapp.budget_tool.model.User;
import com.stephenlindstrom.financeapp.budget_tool.repository.CategoryRepository;

/**
 * Service implementation for managing categories.
 * Provides CRUD operations and filtering by transaction type.
 */
@Service
public class CategoryServiceImpl implements CategoryService {

  private final CategoryRepository categoryRepository;
  private final UserService userService;

  public CategoryServiceImpl(CategoryRepository categoryRepository, UserService userService) {
    this.categoryRepository = categoryRepository;
    this.userService = userService;
  }

  /**
   * Creates a new category.
   *
   * @param dto the data for the new category
   * @return the created CategoryDTO
   */
  @Override
  public CategoryDTO create(CategoryCreateDTO dto) {
    User user = userService.getAuthenticatedUser();    
    Category category = mapToEntity(dto, user);
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
    User user = userService.getAuthenticatedUser();

    return categoryRepository.findByUserOrderByName(user).stream()
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
    User user = userService.getAuthenticatedUser();

    return categoryRepository.findByIdAndUser(id, user)
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
    User user = userService.getAuthenticatedUser();

    Category category = categoryRepository.findByIdAndUser(id, user)
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
    User user = userService.getAuthenticatedUser();
    categoryRepository.deleteByIdAndUser(id, user);
  }

  /**
   * Checks if a category exists by name (case-insensitive).
   *
   * @param name the category name
   * @return true if a category exists, false otherwise
   */
  @Override
  public boolean existsByNameIgnoreCase(String name) {
    User user = userService.getAuthenticatedUser();
    return categoryRepository.existsByNameIgnoreCaseAndUser(name, user);
  }

  /**
   * Retrieves all categories of a specific transaction type (INCOME or EXPENSE).
   *
   * @param type the transaction type to filter by
   * @return list of CategoryDTOs matching the type
   */
  @Override
  public List<CategoryDTO> getByType(TransactionType type) {
    User user = userService.getAuthenticatedUser();

    return categoryRepository.findByUserOrderByName(user).stream()
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
  private Category mapToEntity(CategoryCreateDTO dto, User user) {
    return Category.builder()
            .name(dto.getName())
            .type(dto.getType())
            .user(user)
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