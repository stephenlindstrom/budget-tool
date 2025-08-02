package com.stephenlindstrom.financeapp.budget_tool.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserRegistrationDTO {

  @Schema(description = "New user's username", example = "newUser123")
  @NotBlank(message = "Username required")
  private String username;

  @Schema(description = "New user's password", example = "securePassword")
  @NotBlank(message = "Password required")
  private String password;
}
