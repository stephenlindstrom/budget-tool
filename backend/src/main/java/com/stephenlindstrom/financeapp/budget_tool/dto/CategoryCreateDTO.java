package com.stephenlindstrom.financeapp.budget_tool.dto;

import com.stephenlindstrom.financeapp.budget_tool.enums.TransactionType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CategoryCreateDTO {
  @Schema(description = "Name of the category", example = "Groceries")
  @NotBlank
  private String name;

  @Schema(description = "Transaction type", example = "EXPENSE", allowableValues = {"INCOME", "EXPENSE"})
  @NotNull
  private TransactionType type;
}
