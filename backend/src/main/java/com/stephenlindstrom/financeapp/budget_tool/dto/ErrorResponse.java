package com.stephenlindstrom.financeapp.budget_tool.dto;

import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "Error response object")
public class ErrorResponse {
  @Schema(description = "Error message", example = "Resource not found")
  private String message;

  @Schema(description = "Field-specific validation errors", example = "{\"username\": \"Username must not be blank\"}")
  private Map<String, String> errors;

  public ErrorResponse(String message) {
    this.message = message;
  }

  public ErrorResponse(String message, Map<String, String> errors) {
    this.message = message;
    this.errors = errors;
  }
}
