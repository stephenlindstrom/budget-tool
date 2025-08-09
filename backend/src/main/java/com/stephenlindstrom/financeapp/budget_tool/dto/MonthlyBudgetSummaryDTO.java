package com.stephenlindstrom.financeapp.budget_tool.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
/**
 * Monthly wrapper that provides the month metadata and the list
 * of budget summaries for that month.
 */
@Builder
@Data
public class MonthlyBudgetSummaryDTO {
  @Schema(description="Month info (machine-friendly and human-friendly strings)")
  private MonthDTO monthDTO;

  @Schema(description = "Collection of budget summaries for the month")
  private List<BudgetSummaryDTO> budgetSummaryDTOs;
}
