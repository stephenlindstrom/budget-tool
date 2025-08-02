package com.stephenlindstrom.financeapp.budget_tool.controller;

import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stephenlindstrom.financeapp.budget_tool.service.JwtService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

/**
 * REST controller for providing demo JWT token
 */
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
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Demo JWT token generated",
      content = @Content(
        schema = @Schema(example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiO...")
      ))
  })
  @GetMapping("/demo-token")
  public ResponseEntity<String> getDemoToken() {
    String token = jwtService.generateToken("demoUser");
    return ResponseEntity.ok(token);
  }

}
