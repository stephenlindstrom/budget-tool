package com.stephenlindstrom.financeapp.budget_tool.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.stephenlindstrom.financeapp.budget_tool.enums.TransactionType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TransactionCreateDTO {

  @Schema(description = "Transaction amount", example = "98.57", minimum = "0")
  @NotNull
  @PositiveOrZero
  private BigDecimal amount;

  @Schema(description = "Category ID that transaction belongs to", example = "1")
  @NotNull
  private Long categoryId;

  @Schema(description = "Transaction type", example = "EXPENSE", allowableValues = {"INCOME", "EXPENSE"})
  @NotNull
  private TransactionType type;

  @Schema(description = "Transaction date in yyyy-MM-dd format", example = "2025-04-22")
  private LocalDate date;

  @Schema(description = "Brief description of transaction", example = "Coffee at Starbucks")
  private String description;

}
