package com.stephenlindstrom.financeapp.budget_tool.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "Error response object")
public class ErrorResponse {
  @Schema(description = "Error message", example = "Resource not found")
  private String message;
}
