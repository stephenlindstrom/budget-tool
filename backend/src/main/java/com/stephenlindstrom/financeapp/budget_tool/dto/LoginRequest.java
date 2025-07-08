package com.stephenlindstrom.financeapp.budget_tool.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LoginRequest {
  @NotNull
  private String username;

  @NotNull
  private String password;
}
