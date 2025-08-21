package com.stephenlindstrom.financeapp.budget_tool.dto;

import java.math.BigDecimal;
import java.time.YearMonth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class MonthlyOverviewDTO {
  @Schema(example = "2025-08")
  private YearMonth month;

  @Schema(example = "3200.00")
  private BigDecimal budgeted;

  @Schema(example = "2415.33")
  private BigDecimal spent;

  @Schema(example = "784.67")
  private BigDecimal remaining;

  @Schema(example = "4200.00")
  private BigDecimal income;
}
