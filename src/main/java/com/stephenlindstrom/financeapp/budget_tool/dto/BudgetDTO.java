package com.stephenlindstrom.financeapp.budget_tool.dto;

import java.math.BigDecimal;
import java.time.YearMonth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class BudgetDTO {
  @Schema(description = "Unique identifier of the budget", example = "1")
  private Long id;

  @Schema(description = "Amount budgeted", example = "500.00")
  private BigDecimal value;

  @Schema(description = "Year and month of budget", example = "2025-02", type = "string", pattern = "yyyy-MM")
  private YearMonth month;

  @Schema(description = "Category DTO that budget belongs to")
  private CategoryDTO category;
}
