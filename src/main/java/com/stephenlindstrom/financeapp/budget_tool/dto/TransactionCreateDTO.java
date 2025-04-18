package com.stephenlindstrom.financeapp.budget_tool.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.stephenlindstrom.financeapp.budget_tool.enums.TransactionType;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TransactionCreateDTO {
  @NotNull
  @PositiveOrZero
  private BigDecimal amount;

  @NotNull
  private Long categoryId;

  @NotNull
  private TransactionType type;

  private LocalDate date;

  private String description;

}
