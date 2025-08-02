package com.stephenlindstrom.financeapp.budget_tool.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stephenlindstrom.financeapp.budget_tool.dto.ErrorResponse;
import com.stephenlindstrom.financeapp.budget_tool.dto.LoginRequest;
import com.stephenlindstrom.financeapp.budget_tool.dto.SuccessResponse;
import com.stephenlindstrom.financeapp.budget_tool.dto.UserRegistrationDTO;
import com.stephenlindstrom.financeapp.budget_tool.service.JwtService;
import com.stephenlindstrom.financeapp.budget_tool.service.UserService;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
/**
 * REST controller for authentication-related endpoints.
 * Handles requests for registering new users and logging in users.
 * 
 * Base route: /api/auth
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final UserService userService;
  private final JwtService jwtService;

  public AuthController(UserService userService, JwtService jwtService) {
    this.userService = userService;
    this.jwtService = jwtService;
  }

  @Operation(
    summary = "Register a new user",
    description = "Register a new user with a username and password."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "User registered successfully",
      content = @Content(
        mediaType = "application/json",
        schema = @Schema(implementation = SuccessResponse.class),
        examples = @ExampleObject(name = "RegistrationSuccess", value = """
            {
              "message": "User registered successfully",
              "token": null
            }
            """
        )
      )
    ),
    @ApiResponse(responseCode = "400", description = "Missing username or password",
      content = @Content(
        mediaType = "application/json",
        schema = @Schema(implementation = ErrorResponse.class),
        examples = @ExampleObject(name = "MissingInputError", value = """
            {
              "message": "Validation failed",
              "errors": {
                "password": "Password required",
                "username": "Username required"
              }
            }
            """
        )
      )
    )
  })
  @PostMapping("/register")
  public ResponseEntity<SuccessResponse> register(@RequestBody @Valid UserRegistrationDTO dto) {
    userService.registerUser(dto);
    return ResponseEntity.status(HttpStatus.CREATED).body(new SuccessResponse("User registered successfully"));
  }

  @Operation(
    summary = "Login registered user",
    description = "Login registered user by providing valid username and password."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Login successful and JWT token returned",
      content = @Content(
        mediaType = "application/json",
        schema = @Schema(implementation = SuccessResponse.class),
        examples = @ExampleObject(name = "LoginSuccess", value = """
            {
              "message": "Login successful",
              "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJkZW1v..."
            }
            """)
      )
    ),
    @ApiResponse(responseCode = "400", description = "Missing username or password",
      content = @Content(
        mediaType = "application/json",
        schema = @Schema(implementation = ErrorResponse.class),
        examples = @ExampleObject(name = "MissingInputError", value = """
            {
              "message": "Validation failed",
              "errors": {
                "password": "Password required",
                "username": "Username required"
              }
            }
            """
        )
      )
    ),
    @ApiResponse(responseCode = "401", description = "Invalid username or password", 
      content = @Content(schema = @Schema(implementation = String.class)))
  })
  @PostMapping("/login")
  public ResponseEntity<SuccessResponse> login(@RequestBody @Valid LoginRequest loginRequest) {
      userService.authenticateUser(loginRequest.getUsername(), loginRequest.getPassword());
      String token = jwtService.generateToken(loginRequest.getUsername());
      return ResponseEntity.ok(new SuccessResponse("Login successful", token));
  }
}
