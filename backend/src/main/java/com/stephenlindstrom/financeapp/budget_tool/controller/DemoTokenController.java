package com.stephenlindstrom.financeapp.budget_tool.controller;

import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stephenlindstrom.financeapp.budget_tool.service.JwtService;

import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/api/auth")
@Profile("demo")
public class DemoTokenController {

  private final JwtService jwtService;

  public DemoTokenController(JwtService jwtService) {
    this.jwtService = jwtService;
  }

  @Operation(summary = "Get JWT token for demo user", 
            description = "Returns a JWT token for 'demoUser'. Only available in 'demo' profile.")
  @GetMapping("/demo-token")
  public ResponseEntity<String> getDemoToken() {
    String token = jwtService.generateToken("demoUser");
    return ResponseEntity.ok(token);
  }

}
