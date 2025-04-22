package com.stephenlindstrom.financeapp.budget_tool.dto;

import java.math.BigDecimal;
import java.time.YearMonth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class BudgetCreateDTO {
  @Schema(description = "Budget amount", example = "550.50", minimum = "0")
  @NotNull
  @PositiveOrZero
  private BigDecimal value;

  @Schema(description = "Year and month of budget", example = "2025-01", type = "string", pattern = "yyyy-MM")
  @NotNull
  private YearMonth month;

  @Schema(description = "Category ID that budget belongs to", example = "1")
  @NotNull
  private Long categoryId;
}
