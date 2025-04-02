package com.stephenlindstrom.financeapp.budget_tool.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stephenlindstrom.financeapp.budget_tool.dto.TransactionFilter;
import com.stephenlindstrom.financeapp.budget_tool.model.Transaction;
import com.stephenlindstrom.financeapp.budget_tool.service.TransactionService;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

  private final TransactionService transactionService;

  public TransactionController(TransactionService transactionService) {
    this.transactionService = transactionService;
  }

  @PostMapping
  public ResponseEntity<Transaction> create(@RequestBody Transaction transaction) {
    return ResponseEntity.ok(transactionService.save(transaction));
  }

  @GetMapping
  public ResponseEntity<List<Transaction>> getAll() {
    return ResponseEntity.ok(transactionService.getAll());
  }

  @GetMapping("/filter")
  public ResponseEntity<List<Transaction>> filter(@ModelAttribute TransactionFilter filter) {
    List<Transaction> results = transactionService.filter(filter);
    return ResponseEntity.ok(results);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    transactionService.deleteById(id);
    return ResponseEntity.noContent().build();
  }
}
