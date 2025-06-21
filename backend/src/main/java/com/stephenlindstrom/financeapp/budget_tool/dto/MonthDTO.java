package com.stephenlindstrom.financeapp.budget_tool.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MonthDTO {
  @Schema(description = "Machine-friendly string for URLs and API calls", example = "2025-03")
  private String value;

  @Schema(description = "Human-friendly string for user interface", example = "March 2025")
  private String display;
}
