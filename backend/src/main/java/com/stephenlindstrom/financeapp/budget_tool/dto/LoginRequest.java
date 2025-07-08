package com.stephenlindstrom.financeapp.budget_tool.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
  @NotBlank
  private String username;

  @NotBlank
  private String password;
}
