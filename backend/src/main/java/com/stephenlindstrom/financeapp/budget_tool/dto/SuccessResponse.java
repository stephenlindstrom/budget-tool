package com.stephenlindstrom.financeapp.budget_tool.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SuccessResponse {
  private String message;

  private String token;

  public SuccessResponse(String message) {
    this.message = message;
  }

  public SuccessResponse(String message, String token) {
    this.message = message;
    this.token = token;
  }
}
