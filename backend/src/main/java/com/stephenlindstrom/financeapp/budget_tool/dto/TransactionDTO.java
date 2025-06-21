package com.stephenlindstrom.financeapp.budget_tool.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.stephenlindstrom.financeapp.budget_tool.enums.TransactionType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TransactionDTO {
  @Schema(description = "Unique identifer of the transaction", example = "1")
  private Long id;

  @Schema(description = "Transaction amount", example = "34.32")
  private BigDecimal amount;

  @Schema(description = "Category DTO that transaction belongs to")
  private CategoryDTO category;

  @Schema(description = "Transaction type", example = "EXPENSE", allowableValues = {"INCOME", "EXPENSE"})
  private TransactionType type;

  @Schema(description = "Transaction date in yyyy-MM-dd format", example = "2025-04-22")
  private LocalDate date;

  @Schema(description = "Brief description of transaction", example = "Coffee at Starbucks")
  private String description;
}
