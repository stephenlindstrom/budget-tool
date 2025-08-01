package com.stephenlindstrom.financeapp.budget_tool.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stephenlindstrom.financeapp.budget_tool.dto.LoginRequest;
import com.stephenlindstrom.financeapp.budget_tool.dto.UserRegistrationDTO;
import com.stephenlindstrom.financeapp.budget_tool.service.JwtService;
import com.stephenlindstrom.financeapp.budget_tool.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
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
  @PostMapping("/register")
  public ResponseEntity<String> register(@RequestBody @Valid UserRegistrationDTO dto) {
    try {
      userService.registerUser(dto);
      return ResponseEntity.ok("User registered successfully");
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  @Operation(
    summary = "Login registered user",
    description = "Login registered user by providing valid username and password."
  )
  @PostMapping("/login")
  public ResponseEntity<String> login(@RequestBody @Valid LoginRequest loginRequest) {
    try {
        userService.authenticateUser(loginRequest.getUsername(), loginRequest.getPassword());
        String token = jwtService.generateToken(loginRequest.getUsername());
        return ResponseEntity.ok(token);
    } catch (IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
  }
}
