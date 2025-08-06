package com.stephenlindstrom.financeapp.budget_tool.service;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import com.stephenlindstrom.financeapp.budget_tool.dto.BudgetCreateDTO;
import com.stephenlindstrom.financeapp.budget_tool.dto.BudgetDTO;
import com.stephenlindstrom.financeapp.budget_tool.dto.BudgetSummaryDTO;
import com.stephenlindstrom.financeapp.budget_tool.dto.MonthDTO;

/**
 * Service interface for managing budgets.
 * Defines the contract for creating, retrieving, updating, and deleting budgets.
 */
public interface BudgetService {

    /**
     * Creates a new budget entry.
     *
     * @param dto the data for the new budget
     * @return the created BudgetDTO
     */
    BudgetDTO create(BudgetCreateDTO dto);

    /**
     * Retrieves all budgets, sorted in descending order by month.
     *
     * @return list of all BudgetDTOs
     */
    List<BudgetDTO> getAll();

    /**
     * Retrieves a budget by its ID.
     *
     * @param id the ID of the budget
     * @return an Optional containing the BudgetDTO if found
     */
    Optional<BudgetDTO> getById(Long id);

    /**
     * Updates an existing budget by ID.
     *
     * @param id the ID of the budget to update
     * @param dto the new budget data
     * @return the updated BudgetDTO
     */
    BudgetDTO updateById(Long id, BudgetCreateDTO dto);

    /**
     * Deletes a budget by its ID.
     *
     * @param id the ID of the budget to delete
     */
    void deleteById(Long id);

    /**
     * Checks if a budget exists for a given category and month.
     *
     * @param categoryId the ID of the category
     * @param month the month to check
     * @return true if a budget exists, false otherwise
     */
    boolean existsByCategoryIdAndMonth(Long categoryId, YearMonth month);

    /**
     * Retrieves a summary of the budget, including amount spent and remaining.
     *
     * @param id the ID of the budget
     * @return the budget summary
     */
    BudgetSummaryDTO getBudgetSummary(Long id);

    /**
   * Generates budget summaries for all budgets in a given month.
   * 
   * @param month the year-month to filter budgets by
   * @return a list of summary DTOs for each budget
   */
    List<BudgetSummaryDTO> getMonthlyBudgetSummaries(YearMonth month);

    /**
     * Retrieves all budgets for a specific month.
     *
     * @param month the month to filter by
     * @return list of BudgetDTOs for the given month
     */
    List<BudgetDTO> getByMonth(YearMonth month);

    /**
     * Retrieves a list of months for which budget data exists.
     * Sorted in reverse chronological order.
     *
     * @return list of MonthDTOs representing available months
     */
    List<MonthDTO> getAvailableMonths();
}