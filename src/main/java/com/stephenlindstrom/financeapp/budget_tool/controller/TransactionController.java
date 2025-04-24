package com.stephenlindstrom.financeapp.budget_tool.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stephenlindstrom.financeapp.budget_tool.dto.TransactionCreateDTO;
import com.stephenlindstrom.financeapp.budget_tool.dto.TransactionDTO;
import com.stephenlindstrom.financeapp.budget_tool.dto.TransactionFilter;
import com.stephenlindstrom.financeapp.budget_tool.service.TransactionService;

import io.swagger.v3.oas.annotations.Operation;
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
  @PostMapping
  public ResponseEntity<TransactionDTO> create(@RequestBody @Valid TransactionCreateDTO dto) {
    TransactionDTO created = transactionService.save(dto);
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
  }

  @Operation(
    summary = "Get all transactions",
    description = "Returns a list of all transactions"
  )
  @GetMapping
  public ResponseEntity<List<TransactionDTO>> getAll() {
    return ResponseEntity.ok(transactionService.getAll());
  }

  @Operation(
    summary = "Get filtered transactions",
    description = "Returns a list of transactions optionally filtered by transaction type, category, start date, and/or end date"
  )
  @GetMapping("/filter")
  public ResponseEntity<List<TransactionDTO>> filter(@ModelAttribute TransactionFilter filter) {
    List<TransactionDTO> results = transactionService.filter(filter);
    return ResponseEntity.ok(results);
  }

  @Operation(
    summary = "Delete a transaction by ID"
  )
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    transactionService.deleteById(id);
    return ResponseEntity.noContent().build();
  }
}
