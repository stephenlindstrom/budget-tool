package com.stephenlindstrom.financeapp.budget_tool.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserRegistrationDTO {

  @Schema(description = "New user's username", example = "newUser123")
  @NotBlank(message = "Username required")
  @Size(min = 3, max = 50, message = "Username must be 3-50 characters")
  private String username;

  @Schema(description = "New user's password", example = "securePassword")
  @NotBlank(message = "Password required")
  @Size(min = 8, max = 72, message = "Password must be at least 8 characters")
  private String password;

  @Schema(description = "Confirming new password", example = "securePassword")
  @NotBlank(message = "Password confirmation required")
  private String confirmPassword;

  @AssertTrue(message = "Passwords do not match")
  public boolean isPasswordConfirmed() {
    return password != null && password.equals(confirmPassword);
  }
}
