package com.stephenlindstrom.financeapp.budget_tool;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BudgetToolApplication {

	public static void main(String[] args) {
    // Load the .env file
    Dotenv dotenv = Dotenv.load();

    // Set system properties so Spring Boot can access them
    System.setProperty("DB_URL", dotenv.get("DB_URL"));
    System.setProperty("DB_USERNAME", dotenv.get("DB_USERNAME"));
    System.setProperty("DB_PASSWORD", dotenv.get("DB_PASSWORD"));
    
		SpringApplication.run(BudgetToolApplication.class, args);
	}

}
