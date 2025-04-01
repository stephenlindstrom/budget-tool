package com.stephenlindstrom.financeapp.budget_tool;

import com.stephenlindstrom.financeapp.budget_tool.model.TestEntity;
import com.stephenlindstrom.financeapp.budget_tool.repository.TestRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

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
