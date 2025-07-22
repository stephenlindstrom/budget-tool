package com.stephenlindstrom.financeapp.budget_tool.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserRegistrationDTO {
  @NotBlank
  private String username;

  @NotBlank
  private String password;
}
