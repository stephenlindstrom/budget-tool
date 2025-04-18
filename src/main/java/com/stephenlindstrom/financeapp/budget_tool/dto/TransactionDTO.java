package com.stephenlindstrom.financeapp.budget_tool.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.stephenlindstrom.financeapp.budget_tool.enums.TransactionType;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TransactionDTO {
  private Long id;
  private BigDecimal amount;
  private CategoryDTO category;
  private TransactionType type;
  private LocalDate date;
  private String description;
}
