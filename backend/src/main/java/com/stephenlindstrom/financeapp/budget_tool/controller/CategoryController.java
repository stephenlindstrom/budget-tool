package com.stephenlindstrom.financeapp.budget_tool.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stephenlindstrom.financeapp.budget_tool.dto.CategoryCreateDTO;
import com.stephenlindstrom.financeapp.budget_tool.dto.CategoryDTO;
import com.stephenlindstrom.financeapp.budget_tool.service.CategoryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Category created successfully"),
    @ApiResponse(responseCode = "400", description = "Invalid input data"),
    @ApiResponse(responseCode = "500", description = "Server error")
  })
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
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Categories found and returned"),
    @ApiResponse(responseCode = "500", description = "Server error")
  })
  @GetMapping
  public ResponseEntity<List<CategoryDTO>> getAll() {
    return ResponseEntity.ok(categoryService.getAll());
  }

  @Operation(
    summary = "Get a category by ID",
    description = "Returns a category with a certain ID or a not found response"
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Category found and returned"),
    @ApiResponse(responseCode = "404", description = "Category not found"),
    @ApiResponse(responseCode = "500", description = "Server error")
  })
  @GetMapping("/{id}")
  public ResponseEntity<CategoryDTO> getById(@PathVariable Long id) {
    return categoryService.getById(id)
          .map(ResponseEntity::ok)
          .orElse(ResponseEntity.notFound().build());
  }

  @Operation(
    summary = "Update a category by ID",
    description = "Updates an existing category with the specified ID"
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Category updated successfully"),
    @ApiResponse(responseCode = "404", description = "Category not found"),
    @ApiResponse(responseCode = "400", description = "Invalid input data"),
    @ApiResponse(responseCode = "500", description = "Server error")
  })
  @PutMapping("/{id}")
  public ResponseEntity<CategoryDTO> updateById(@PathVariable Long id, @RequestBody @Valid CategoryCreateDTO dto) {
    CategoryDTO updated = categoryService.updateById(id, dto);
    return ResponseEntity.ok(updated);
  }

  @Operation(
    summary = "Delete a category by ID",
    description = "Deletes the category with the specified ID"
  )
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Category deleted successfully"),
    @ApiResponse(responseCode = "404", description = "Category not found"),
    @ApiResponse(responseCode = "500", description = "Server error")
  })
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    categoryService.deleteById(id);
    return ResponseEntity.noContent().build();
  }
}
