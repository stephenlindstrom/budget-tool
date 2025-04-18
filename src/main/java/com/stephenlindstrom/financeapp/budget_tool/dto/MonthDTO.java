package com.stephenlindstrom.financeapp.budget_tool.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MonthDTO {
  private String value;
  private String display;
}
