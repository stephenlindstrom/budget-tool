package com.stephenlindstrom.financeapp.budget_tool.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

  @Schema(description = "Username of the user", example = "demoUser")
  @NotBlank(message = "Username required")
  private String username;

  @Schema(description = "Password of the user", example = "demoPassword")
  @NotBlank(message = "Password required")
  private String password;
}
