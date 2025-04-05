package com.stephenlindstrom.financeapp.budget_tool.dto;

import java.math.BigDecimal;
import java.time.YearMonth;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class BudgetDTO {
  private Long id;
  private BigDecimal value;
  private YearMonth month;
  private CategoryDTO category;
}
