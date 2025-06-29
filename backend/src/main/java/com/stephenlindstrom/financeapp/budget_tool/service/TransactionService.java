package com.stephenlindstrom.financeapp.budget_tool.service;

import java.util.List;

import com.stephenlindstrom.financeapp.budget_tool.dto.TransactionCreateDTO;
import com.stephenlindstrom.financeapp.budget_tool.dto.TransactionDTO;
import com.stephenlindstrom.financeapp.budget_tool.dto.TransactionFilter;

/**
 * Service interface for managing transactions.
 * Defines operations for creating, retrieving, filtering, updating, and deleting transactions.
 */
public interface TransactionService {

    /**
     * Creates a new transaction.
     *
     * @param dto the data for the new transaction
     * @return the created TransactionDTO
     */
    TransactionDTO create(TransactionCreateDTO dto);

    /**
     * Retrieves all transactions, sorted in descending order by date.
     *
     * @return list of all TransactionDTOs
     */
    List<TransactionDTO> getAll();

    /**
     * Filters transactions based on criteria such as type, category, and date range.
     *
     * @param filter the filter parameters
     * @return list of TransactionDTOs matching the filter
     */
    List<TransactionDTO> filter(TransactionFilter filter);

    /**
     * Updates an existing transaction by ID.
     *
     * @param id the ID of the transaction to update
     * @param dto the new transaction data
     * @return the updated TransactionDTO
     */
    TransactionDTO updateById(Long id, TransactionCreateDTO dto);

    /**
     * Deletes a transaction by its ID.
     *
     * @param id the ID of the transaction to delete
     */
    void deleteById(Long id);
}