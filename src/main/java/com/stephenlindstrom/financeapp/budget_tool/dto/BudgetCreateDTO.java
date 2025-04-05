package com.stephenlindstrom.financeapp.budget_tool.dto;

import java.math.BigDecimal;
import java.time.YearMonth;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class BudgetCreateDTO {
  @NotNull
  @PositiveOrZero
  private BigDecimal value;

  @NotNull
  private YearMonth month;

  @NotNull
  private Long categoryId;
}
