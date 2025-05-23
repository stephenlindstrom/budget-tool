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
import com.stephenlindstrom.financeapp.budget_tool.dto.MonthDTO;
import com.stephenlindstrom.financeapp.budget_tool.service.BudgetService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/budgets")
public class BudgetController {

  private final BudgetService budgetService;

  public BudgetController(BudgetService budgetService) {
    this.budgetService = budgetService;
  }

  @Operation(
    summary = "Create a new budget",
    description = "Create a new budget with an amount, a month and year, and associated category"
  )
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Budget created successfully"),
    @ApiResponse(responseCode = "400", description = "Invalid input data"),
    @ApiResponse(responseCode = "500", description = "Server error")
  })
  @PostMapping
  public ResponseEntity<BudgetDTO> create(@RequestBody @Valid BudgetCreateDTO dto) {
    BudgetDTO created = budgetService.create(dto);
    return ResponseEntity
          .status(HttpStatus.CREATED)
          .body(created);
  }

  @Operation(
    summary = "Get all budgets",
    description = "Returns a list of all budgets"
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Budgets found and returned"),
    @ApiResponse(responseCode = "500", description = "Server error")
  })
  @GetMapping
  public ResponseEntity<List<BudgetDTO>> getAll() {
    return ResponseEntity.ok(budgetService.getAll());
  }

  @Operation(
    summary = "Get budget by ID",
    description = "Returns the budget with the given ID, or a 404 if not found"
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Budget found and returned"),
    @ApiResponse(responseCode = "404", description = "Budget not found"),
    @ApiResponse(responseCode = "500", description = "Server error")
  })
  @GetMapping("/{id}")
  public ResponseEntity<BudgetDTO> getById(@PathVariable Long id) {
    return budgetService.getById(id)
          .map(ResponseEntity::ok)
          .orElse(ResponseEntity.notFound().build());
  }

  @Operation(
    summary = "Get available months with saved budgets",
    description = "Returns a list of all year-month combinations that have saved budget records"
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Months found and returned"),
    @ApiResponse(responseCode = "500", description = "Server error")
  })
  @GetMapping("/months")
  public ResponseEntity<List<MonthDTO>> getAvailableMonths() {
    return ResponseEntity.ok(budgetService.getAvailableMonths());
  } 

  @Operation(
    summary = "Delete a budget by ID",
    description = "Deletes a budget with the specified ID"
  )
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Budget deleted successfully"),
    @ApiResponse(responseCode = "404", description = "Budget not found"),
    @ApiResponse(responseCode = "500", description = "Server error")
  })
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteById(@PathVariable Long id) {
    budgetService.deleteById(id);
    return ResponseEntity.noContent().build();
  }
}
