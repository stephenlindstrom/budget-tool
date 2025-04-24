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

import com.stephenlindstrom.financeapp.budget_tool.dto.CategoryCreateDTO;
import com.stephenlindstrom.financeapp.budget_tool.dto.CategoryDTO;
import com.stephenlindstrom.financeapp.budget_tool.service.CategoryService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

  private final CategoryService categoryService;

  public CategoryController(CategoryService categoryService) {
    this.categoryService = categoryService;
  }

  @Operation(
    summary = "Create a category",
    description = "Creates a category with a given name and transaction type"
  )
  @PostMapping
  public ResponseEntity<CategoryDTO> create(@RequestBody @Valid CategoryCreateDTO dto) {
    CategoryDTO created = categoryService.create(dto);
    return ResponseEntity
          .status(HttpStatus.CREATED)
          .body(created);
  }

  @Operation(
    summary= "Get all categories",
    description = "Returns a list of all categories"
  )
  @GetMapping
  public ResponseEntity<List<CategoryDTO>> getAll() {
    return ResponseEntity.ok(categoryService.getAll());
  }

  @Operation(
    summary = "Get a category by ID",
    description = "Returns a category with a certain ID or a not found response"
  )
  @GetMapping("/{id}")
  public ResponseEntity<CategoryDTO> getById(@PathVariable Long id) {
    return categoryService.getById(id)
          .map(ResponseEntity::ok)
          .orElse(ResponseEntity.notFound().build());
  }

  @Operation(
    summary = "Delete a category by ID"
  )
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    categoryService.deleteById(id);
    return ResponseEntity.noContent().build();
  }
}
