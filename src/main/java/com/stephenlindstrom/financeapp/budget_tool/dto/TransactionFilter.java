package com.stephenlindstrom.financeapp.budget_tool.dto;

import java.time.LocalDate;

import com.stephenlindstrom.financeapp.budget_tool.enums.TransactionType;

import lombok.Data;

@Data
public class TransactionFilter {
  private TransactionType type;
  private Long categoryId;
  private LocalDate startDate;
  private LocalDate endDate;
}
