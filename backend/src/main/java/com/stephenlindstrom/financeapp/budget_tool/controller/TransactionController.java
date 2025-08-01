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

import com.stephenlindstrom.financeapp.budget_tool.dto.ErrorResponse;
import com.stephenlindstrom.financeapp.budget_tool.dto.TransactionCreateDTO;
import com.stephenlindstrom.financeapp.budget_tool.dto.TransactionDTO;
import com.stephenlindstrom.financeapp.budget_tool.dto.TransactionFilter;
import com.stephenlindstrom.financeapp.budget_tool.service.TransactionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;

/**
 * REST controller for managing transactions.
 * Provides endpoints to create, retrieve, filter, update, and delete transactions.
 *
 * Base route: /api/transactions
 */
@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

  private final TransactionService transactionService;

  public TransactionController(TransactionService transactionService) {
    this.transactionService = transactionService;
  }

  @Operation(
    summary = "Create a transaction",
    description = "Creates a transaction with a given amount, category, transaction type, date, and description."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Transaction created successfully"),
    @ApiResponse(responseCode = "400", description = "Invalid input data",
      content = @Content(
        mediaType = "application/json",
        schema = @Schema(implementation = ErrorResponse.class),
        examples = @ExampleObject(name = "Validation Error", value = "{\"message\": \"Validation failed for request\"}")
      )
    ),
    @ApiResponse(responseCode = "500", description = "Server error",
      content = @Content(
        mediaType = "application/json",
        schema = @Schema(implementation = ErrorResponse.class),
        examples = @ExampleObject(name = "ServerErrorExample", value = "{\"message\": \"An unexpected error occurred\"}")
      )
    )
  })
  @PostMapping
  public ResponseEntity<TransactionDTO> create(@RequestBody @Valid TransactionCreateDTO dto) {
    TransactionDTO created = transactionService.create(dto);
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
  }

  @Operation(
    summary = "Get all transactions",
    description = "Returns a list of all transactions sorted by date with most recent transaction first."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Transactions found and returned"),
    @ApiResponse(responseCode = "500", description = "Server error",
      content = @Content(
        mediaType = "application/json",
        schema = @Schema(implementation = ErrorResponse.class),
        examples = @ExampleObject(name = "ServerErrorExample", value = "{\"message\": \"An unexpected error occurred\"}")
      )
    )
  })
  @GetMapping
  public ResponseEntity<List<TransactionDTO>> getAll() {
    return ResponseEntity.ok(transactionService.getAll());
  }

  @Operation(
    summary = "Get filtered transactions",
    description = "Returns a list of transactions optionally filtered by transaction type, category, start date, and/or end date. Transactions are sorted by date with most recent transaction first."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Transactions found and returned"),
    @ApiResponse(responseCode = "500", description = "Server error", 
      content = @Content(
        mediaType = "application/json",
        schema = @Schema(implementation = ErrorResponse.class),
        examples = @ExampleObject(name = "ServerErrorExample", value = "{\"message\": \"An unexpected error occurred\"}")
      )
    )
  })
  @GetMapping("/filter")
  public ResponseEntity<List<TransactionDTO>> filter(@ModelAttribute TransactionFilter filter) {
    List<TransactionDTO> results = transactionService.filter(filter);
    return ResponseEntity.ok(results);
  }

  @Operation(
    summary = "Update a transaction by ID",
    description = "Updates an existing transaction with the specified ID."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Transaction updated successfully"),
    @ApiResponse(responseCode = "400", description = "Invalid input data",
      content = @Content(
        mediaType = "application/json",
        schema = @Schema(implementation = ErrorResponse.class),
        examples = @ExampleObject(name = "Validation Error", value = "{\"message\": \"Validation failed for request\"}")
      )   
    ),
    @ApiResponse(responseCode = "404", description = "Transaction or category not found",
      content = @Content(
        mediaType = "application/json",
        schema = @Schema(implementation = ErrorResponse.class),
        examples = @ExampleObject(name = "NotFoundExample", value = "{\"message\": \"Resource not found\"}")
      )
    ),
    @ApiResponse(responseCode = "500", description = "Server error",
      content = @Content(
        mediaType = "application/json",
        schema = @Schema(implementation = ErrorResponse.class),
        examples = @ExampleObject(name = "ServerErrorExample", value = "{\"message\": \"An unexpected error occurred\"}")
      )
    )
  })
  @PutMapping("/{id}")
  public ResponseEntity<TransactionDTO> updateById(
    @Parameter(description = "ID of the transaction to update")
    @PathVariable Long id, 
    @RequestBody @Valid TransactionCreateDTO dto
  ) {
    TransactionDTO updated = transactionService.updateById(id, dto);
    return ResponseEntity.ok(updated);
  }

  @Operation(
    summary = "Delete a transaction by ID",
    description = "Deletes a transaction with specified ID."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Transaction deleted successfully"),
    @ApiResponse(responseCode = "500", description = "Server error",
      content = @Content(
        mediaType = "application/json",
        schema = @Schema(implementation = ErrorResponse.class),
        examples = @ExampleObject(name = "ServerErrorExample", value = "{\"message\": \"An unexpected error occurred\"}")
      )
    )
  })
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(
    @Parameter(description = "ID of the transaction to delete")
    @PathVariable Long id
  ) {
    transactionService.deleteById(id);
    return ResponseEntity.noContent().build();
  }
}
