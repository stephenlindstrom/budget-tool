package com.stephenlindstrom.financeapp.budget_tool.dto;

import com.stephenlindstrom.financeapp.budget_tool.enums.TransactionType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CategoryCreateDTO {
  @NotBlank
  private String name;

  @NotNull
  private TransactionType type;
}
