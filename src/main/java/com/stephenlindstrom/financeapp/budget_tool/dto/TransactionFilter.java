package com.stephenlindstrom.financeapp.budget_tool.dto;

import java.time.LocalDate;

import com.stephenlindstrom.financeapp.budget_tool.enums.TransactionType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionFilter {
  @Schema(description = "Transaction type", example = "EXPENSE", allowableValues = {"INCOME", "EXPENSE"})
  private TransactionType type;

  @Schema(description = "Category ID that transaction belongs to", example = "4")
  private Long categoryId;

  @Schema(description = "Filter transactions starting from this date", example = "2025-03-23")
  private LocalDate startDate;

  @Schema(description = "Filter transactions up until this date inclusive", example = "2025-04-20")
  private LocalDate endDate;
}
