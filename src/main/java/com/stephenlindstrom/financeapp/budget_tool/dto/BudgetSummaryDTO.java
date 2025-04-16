package com.stephenlindstrom.financeapp.budget_tool.dto;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class BudgetSummaryDTO {
  private BigDecimal budgeted;
  private BigDecimal spent;
  private BigDecimal remaining;
}
