package com.stephenlindstrom.financeapp.budget_tool;

import com.stephenlindstrom.financeapp.budget_tool.model.TestEntity;
import com.stephenlindstrom.financeapp.budget_tool.repository.TestRepository;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@OpenAPIDefinition(
  info = @Info(
    title = "Budget Tool API",
    version = "1.0",
    description = "A personal budgeting API for managing transactions and budgets in various categories"
  )
)

@SpringBootApplication
public class BudgetToolApplication {

  public static void main(String[] args) {
    SpringApplication.run(BudgetToolApplication.class, args);
  }

  @Bean
  CommandLineRunner run(TestRepository repo) {
    return args -> {
      TestEntity test = new TestEntity(null, "PostgreSQL is working!");
      repo.save(test);

      repo.findAll().forEach(entry ->
        System.out.println(entry.getMessage()));
    };
  }
}
