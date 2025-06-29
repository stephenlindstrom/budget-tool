package com.stephenlindstrom.financeapp.budget_tool;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@OpenAPIDefinition(
  info = @Info(
    title = "Budget Tool API",
    version = "1.0",
    description = "A personal budgeting API for managing transactions and budgets in various categories"
  )
)
/**
 * Main entry point for the Budget Tool Spring Boot application
 */
@SpringBootApplication
public class BudgetToolApplication {

  public static void main(String[] args) {
    SpringApplication.run(BudgetToolApplication.class, args);
  }
}
