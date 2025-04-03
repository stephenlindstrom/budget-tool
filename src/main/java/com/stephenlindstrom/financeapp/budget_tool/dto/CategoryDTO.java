package com.stephenlindstrom.financeapp.budget_tool.dto;

import com.stephenlindstrom.financeapp.budget_tool.enums.TransactionType;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class CategoryDTO {
  private Long id;
  private String name;
  private TransactionType type;
}
