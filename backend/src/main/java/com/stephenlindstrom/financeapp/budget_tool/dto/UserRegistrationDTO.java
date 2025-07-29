package com.stephenlindstrom.financeapp.budget_tool.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserRegistrationDTO {
  @NotBlank
  private String username;

  @NotBlank
  private String password;
}
