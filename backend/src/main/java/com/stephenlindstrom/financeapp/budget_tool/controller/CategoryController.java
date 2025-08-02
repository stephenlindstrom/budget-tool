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
import com.stephenlindstrom.financeapp.budget_tool.dto.ErrorResponse;
import com.stephenlindstrom.financeapp.budget_tool.service.CategoryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;

/**
 * REST controller for managing categories.
 * Handles requests for creating, retrieving, updating, and deleting categories.
 *
 * Base route: /api/categories
 */
@RestController
@RequestMapping("/api/categories")
@ApiResponses(value = {
  @ApiResponse(responseCode = "401", description = "Unauthorized - Valid JWT token required",
    content = @Content(
      mediaType = "text/plain",
      examples = @ExampleObject(name = "UnauthorizedError", value = "Unauthorized")
    )
  )
})
public class CategoryController {

  private final CategoryService categoryService;

  public CategoryController(CategoryService categoryService) {
    this.categoryService = categoryService;
  }

  @Operation(
    summary = "Create a category",
    description = "Creates a category with a given name and transaction type."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Category created successfully"),
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
  public ResponseEntity<CategoryDTO> create(@RequestBody @Valid CategoryCreateDTO dto) {
    CategoryDTO created = categoryService.create(dto);
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
  }

  @Operation(
    summary= "Get all categories",
    description = "Returns a list of all categories sorted by name."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Categories found and returned"),
    @ApiResponse(responseCode = "500", description = "Server error",
      content = @Content(
        mediaType = "application/json",
        schema = @Schema(implementation = ErrorResponse.class),
        examples = @ExampleObject(name = "ServerErrorExample", value = "{\"message\": \"An unexpected error occurred\"}")
      )
    )
  })
  @GetMapping
  public ResponseEntity<List<CategoryDTO>> getAll() {
    return ResponseEntity.ok(categoryService.getAll());
  }

  @Operation(
    summary = "Get a category by ID",
    description = "Returns a category with a certain ID or a not found response."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Category found and returned"),
    @ApiResponse(responseCode = "404", description = "Category not found",
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
  @GetMapping("/{id}")
  public ResponseEntity<CategoryDTO> getById(
    @Parameter(description = "ID of the category to retrieve") 
    @PathVariable Long id
  ) {
    return categoryService.getById(id)
          .map(ResponseEntity::ok)
          .orElse(ResponseEntity.notFound().build());
  }

  @Operation(
    summary = "Update a category by ID",
    description = "Updates an existing category with the specified ID."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Category updated successfully"),
    @ApiResponse(responseCode = "400", description = "Invalid input data", 
      content = @Content(
        mediaType = "application/json",
        schema = @Schema(implementation = ErrorResponse.class),
        examples = @ExampleObject(name = "Validation Error", value = "{\"message\": \"Validation failed for request\"}")
      )
    ),
    @ApiResponse(responseCode = "404", description = "Category not found", 
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
  public ResponseEntity<CategoryDTO> updateById(
    @Parameter(description = "ID of the category to update") 
    @PathVariable Long id, 
    @RequestBody @Valid CategoryCreateDTO dto
  ) {
    CategoryDTO updated = categoryService.updateById(id, dto);
    return ResponseEntity.ok(updated);
  }

  @Operation(
    summary = "Delete a category by ID",
    description = "Deletes the category with the specified ID."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Category deleted successfully"),
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
    @Parameter(description = "ID of the category to delete")
    @PathVariable Long id
  ) {
    categoryService.deleteById(id);
    return ResponseEntity.noContent().build();
  }
}
