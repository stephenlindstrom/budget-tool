package com.stephenlindstrom.financeapp.budget_tool.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stephenlindstrom.financeapp.budget_tool.dto.TransactionCreateDTO;
import com.stephenlindstrom.financeapp.budget_tool.dto.TransactionDTO;
import com.stephenlindstrom.financeapp.budget_tool.dto.TransactionFilter;
import com.stephenlindstrom.financeapp.budget_tool.service.TransactionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

  private final TransactionService transactionService;

  public TransactionController(TransactionService transactionService) {
    this.transactionService = transactionService;
  }

  @Operation(
    summary = "Create a transaction",
    description = "Creates a transaction with a given amount, category, transaction type, date, and description"
  )
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Transaction created successfully"),
    @ApiResponse(responseCode = "400", description = "Invalid input data"),
    @ApiResponse(responseCode = "500", description = "Server error")
  })
  @PostMapping
  public ResponseEntity<TransactionDTO> create(@RequestBody @Valid TransactionCreateDTO dto) {
    TransactionDTO created = transactionService.save(dto);
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
  }

  @Operation(
    summary = "Get all transactions",
    description = "Returns a list of all transactions"
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Transactions found and returned"),
    @ApiResponse(responseCode = "500", description = "Server error")
  })
  @GetMapping
  public ResponseEntity<List<TransactionDTO>> getAll() {
    return ResponseEntity.ok(transactionService.getAll());
  }

  @Operation(
    summary = "Get filtered transactions",
    description = "Returns a list of transactions optionally filtered by transaction type, category, start date, and/or end date"
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Transactions found and returned"),
    @ApiResponse(responseCode = "500", description = "Server error")
  })
  @GetMapping("/filter")
  public ResponseEntity<List<TransactionDTO>> filter(@ModelAttribute TransactionFilter filter) {
    List<TransactionDTO> results = transactionService.filter(filter);
    return ResponseEntity.ok(results);
  }

  @Operation(
    summary = "Update a transaction by ID",
    description = "Updates an existing transaction with the specified ID"
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Transaction updated successfully"),
    @ApiResponse(responseCode = "404", description = "Transaction or category not found"),
    @ApiResponse(responseCode = "400", description = "Invalid input data"),
    @ApiResponse(responseCode = "500", description = "Server error")
  })
  @PutMapping("/{id}")
  public ResponseEntity<TransactionDTO> updateById(@PathVariable Long id, @RequestBody @Valid TransactionCreateDTO dto) {
    TransactionDTO updated = transactionService.updateById(id, dto);
    return ResponseEntity.ok(updated);
  }

  @Operation(
    summary = "Delete a transaction by ID",
    description = "Deletes a transaction with specified ID"
  )
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Transaction deleted successfully"),
    @ApiResponse(responseCode = "404", description = "Transaction not found"),
    @ApiResponse(responseCode = "500", description = "Server error")
  })
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    transactionService.deleteById(id);
    return ResponseEntity.noContent().build();
  }
}
