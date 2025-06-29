package com.stephenlindstrom.financeapp.budget_tool.dto;

import java.time.LocalDate;

import com.stephenlindstrom.financeapp.budget_tool.enums.TransactionType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * Represents optional filter criteria for retrieving transactions.
 * Used in /filter endpoint to match by type, category, and/or date range.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionFilter {
  @Schema(description = "Transaction type", example = "EXPENSE", allowableValues = {"INCOME", "EXPENSE"})
  private TransactionType type;

  @Schema(description = "Category ID that transaction belongs to", example = "1")
  private Long categoryId;

  @Schema(description = "Filter transactions starting from this date", example = "2025-06-01")
  private LocalDate startDate;

  @Schema(description = "Filter transactions up until this date inclusive", example = "2025-06-04")
  private LocalDate endDate;
}
