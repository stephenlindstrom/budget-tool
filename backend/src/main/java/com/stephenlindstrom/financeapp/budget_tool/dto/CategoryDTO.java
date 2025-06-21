package com.stephenlindstrom.financeapp.budget_tool.dto;

import com.stephenlindstrom.financeapp.budget_tool.enums.TransactionType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class CategoryDTO {
  @Schema(description = "Unique identifier of the category", example = "5")
  private Long id;

  @Schema(description = "Name of the category", example = "Groceries")
  private String name;

  @Schema(description = "Transaction type", example = "EXPENSE", allowableValues = {"INCOME", "EXPENSE"})
  private TransactionType type;
}
