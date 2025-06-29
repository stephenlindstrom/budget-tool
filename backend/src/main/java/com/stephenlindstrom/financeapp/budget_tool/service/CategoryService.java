package com.stephenlindstrom.financeapp.budget_tool.service;

import java.util.List;
import java.util.Optional;

import com.stephenlindstrom.financeapp.budget_tool.dto.CategoryCreateDTO;
import com.stephenlindstrom.financeapp.budget_tool.dto.CategoryDTO;
import com.stephenlindstrom.financeapp.budget_tool.enums.TransactionType;

/**
 * Service interface for managing categories.
 * Defines operations for creating, retrieving, updating, and deleting categories,
 * as well as querying by name or transaction type.
 */
public interface CategoryService {

    /**
     * Creates a new category.
     *
     * @param dto the data for the new category
     * @return the created CategoryDTO
     */
    CategoryDTO create(CategoryCreateDTO dto);

    /**
     * Retrieves all categories, sorted by name.
     *
     * @return list of all CategoryDTOs
     */
    List<CategoryDTO> getAll();

    /**
     * Retrieves a category by its ID.
     *
     * @param id the ID of the category
     * @return an Optional containing the CategoryDTO if found
     */
    Optional<CategoryDTO> getById(Long id);

    /**
     * Updates an existing category by ID.
     *
     * @param id the ID of the category to update
     * @param dto the new category data
     * @return the updated CategoryDTO
     */
    CategoryDTO updateById(Long id, CategoryCreateDTO dto);

    /**
     * Deletes a category by its ID.
     *
     * @param id the ID of the category to delete
     */
    void deleteById(Long id);

    /**
     * Checks if a category exists by name, ignoring case.
     *
     * @param name the category name to check
     * @return true if a category with the given name exists, false otherwise
     */
    boolean existsByNameIgnoreCase(String name);

    /**
     * Retrieves all categories of a specific transaction type (e.g., INCOME or EXPENSE).
     *
     * @param type the transaction type to filter by
     * @return list of CategoryDTOs with the specified type
     */
    List<CategoryDTO> getByType(TransactionType type);
}