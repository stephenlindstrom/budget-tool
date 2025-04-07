package com.stephenlindstrom.financeapp.budget_tool.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stephenlindstrom.financeapp.budget_tool.dto.BudgetCreateDTO;
import com.stephenlindstrom.financeapp.budget_tool.dto.BudgetDTO;
import com.stephenlindstrom.financeapp.budget_tool.service.BudgetService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/budgets")
public class BudgetController {

  private final BudgetService budgetService;

  public BudgetController(BudgetService budgetService) {
    this.budgetService = budgetService;
  }

  @PostMapping
  public ResponseEntity<BudgetDTO> create(@RequestBody @Valid BudgetCreateDTO dto) {
    BudgetDTO created = budgetService.create(dto);
    return ResponseEntity
          .status(HttpStatus.CREATED)
          .body(created);
  }

  @GetMapping
  public ResponseEntity<List<BudgetDTO>> getAll() {
    return ResponseEntity.ok(budgetService.getAll());
  }

  @GetMapping("/{id}")
  public ResponseEntity<BudgetDTO> getById(@PathVariable Long id) {
    return budgetService.getById(id)
          .map(ResponseEntity::ok)
          .orElse(ResponseEntity.notFound().build());
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteById(@PathVariable Long id) {
    budgetService.deleteById(id);
    return ResponseEntity.noContent().build();
  }
}
