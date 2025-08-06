package com.stephenlindstrom.financeapp.budget_tool.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class BudgetSummaryDTO {
  @Schema(description = "Unique ID of the budget", example = "1")
  private Long id;

  @Schema(description = "Category associated with the budget")
  private CategoryDTO category;

  @Schema(description = "Amount budgeted", example = "500.00")
  private BigDecimal budgeted;

  @Schema(description = "Amount spent", example = "101.50")
  private BigDecimal spent;

  @Schema(description = "Amount remaining after subtracting spent from budgeted", example = "398.50")
  private BigDecimal remaining;
}
