package com.stephenlindstrom.financeapp.budget_tool.dto;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class IncomeSummaryDTO {
  private Long categoryId;
  private String categoryName;
  private BigDecimal amount;
}
