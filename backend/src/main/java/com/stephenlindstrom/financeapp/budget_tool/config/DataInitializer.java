package com.stephenlindstrom.financeapp.budget_tool.config;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.stephenlindstrom.financeapp.budget_tool.enums.TransactionType;
import com.stephenlindstrom.financeapp.budget_tool.model.Budget;
import com.stephenlindstrom.financeapp.budget_tool.model.Category;
import com.stephenlindstrom.financeapp.budget_tool.model.Transaction;
import com.stephenlindstrom.financeapp.budget_tool.model.User;
import com.stephenlindstrom.financeapp.budget_tool.repository.BudgetRepository;
import com.stephenlindstrom.financeapp.budget_tool.repository.CategoryRepository;
import com.stephenlindstrom.financeapp.budget_tool.repository.TransactionRepository;
import com.stephenlindstrom.financeapp.budget_tool.repository.UserRepository;
import com.stephenlindstrom.financeapp.budget_tool.service.JwtService;

@Configuration
@Profile("demo")
public class DataInitializer {

  @Bean
  public CommandLineRunner preloadDemoData(
    UserRepository userRepository,
    PasswordEncoder passwordEncoder,
    JwtService jwtService,
    CategoryRepository categoryRepository,
    BudgetRepository budgetRepository,
    TransactionRepository transactionRepository
  ) {
    return args -> {
      String username = "demoUser";
      String password = "demoPassword";

      // Create or fetch demo user
      User user = userRepository.findByUsername(username)
        .orElseGet(() -> {
          User newUser = User.builder()
                    .username(username)
                    .password(passwordEncoder.encode(password))
                    .build();
        
          return userRepository.save(newUser);
        });
        
        // Print token for demo use
        String jwt = jwtService.generateToken(username);
        System.out.println("Preloaded demo user: " + username + " / " + password);
        System.out.println("Demo JWT token: Bearer " + jwt);

        // Create categories
        Map<String, Category> categories = Stream.of(
          new AbstractMap.SimpleEntry<>("Groceries", TransactionType.EXPENSE),
          new AbstractMap.SimpleEntry<>("Rent", TransactionType.EXPENSE),
          new AbstractMap.SimpleEntry<>("Salary", TransactionType.INCOME),
          new AbstractMap.SimpleEntry<>("Dining", TransactionType.EXPENSE),
          new AbstractMap.SimpleEntry<>("Utilities", TransactionType.EXPENSE),
          new AbstractMap.SimpleEntry<>("Freelance", TransactionType.INCOME),
          new AbstractMap.SimpleEntry<>("Entertainment", TransactionType.EXPENSE),
          new AbstractMap.SimpleEntry<>("Investments", TransactionType.INCOME),
          new AbstractMap.SimpleEntry<>("Travel", TransactionType.EXPENSE),
          new AbstractMap.SimpleEntry<>("Gifts", TransactionType.EXPENSE)
        ).map(entry -> Category.builder()
                .name(entry.getKey())
                .type(entry.getValue())
                .user(user)
                .build())
        .collect(Collectors.toMap(Category::getName, categoryRepository::save));

        // Create budgets
        List<Budget> demoBudgets = new ArrayList<>();

        YearMonth startMonth = YearMonth.of(2024, 6);
        YearMonth endMonth = YearMonth.of(2025, 6);

        for (YearMonth month = startMonth; !month.isAfter(endMonth); month = month.plusMonths(1)) {
          demoBudgets.add(Budget.builder()
                  .month(month)
                  .value(BigDecimal.valueOf(500 + (Math.random() * 100 - 50)))
                  .category(categories.get("Groceries"))
                  .user(user)
                  .build());

          demoBudgets.add(Budget.builder()
                  .month(month)
                  .value(BigDecimal.valueOf(1200))
                  .category(categories.get("Rent"))
                  .user(user)
                  .build());

          demoBudgets.add(Budget.builder()
                  .month(month)
                  .value(BigDecimal.valueOf(3000 + (Math.random() * 400 - 200)))
                  .category(categories.get("Salary"))
                  .user(user)
                  .build());

          demoBudgets.add(Budget.builder()
                  .month(month)
                  .value(BigDecimal.valueOf(150 + (Math.random() * 50)))
                  .category(categories.get("Dining"))
                  .user(user)
                  .build());
          
          demoBudgets.add(Budget.builder()
                  .month(month)
                  .value(BigDecimal.valueOf(100 + (Math.random() * 30)))
                  .category(categories.get("Utilities"))
                  .user(user)
                  .build());
                  
        }

        budgetRepository.saveAll(demoBudgets);
        
        // Create transactions
        List<Transaction> demoTransactions = new ArrayList<>();
        
        Random random = new Random();

        for (YearMonth month = startMonth; !month.isAfter(endMonth); month = month.plusMonths(1)) {
          int year = month.getYear();
          int mon = month.getMonthValue();

          demoTransactions.add(Transaction.builder()
                    .description("Monthly Paycheck")
                    .amount(BigDecimal.valueOf(2900 + random.nextInt(400)))
                    .date(LocalDate.of(year, mon, 1))
                    .category(categories.get("Salary"))
                    .type(categories.get("Salary").getType())
                    .user(user)
                    .build());

          demoTransactions.add(Transaction.builder()
                    .description("Rent Payment")
                    .amount(BigDecimal.valueOf(1200))
                    .date(LocalDate.of(year, mon, 1))
                    .category(categories.get("Rent"))
                    .type(categories.get("Rent").getType())
                    .user(user)
                    .build());

          for (int i = 0; i < 3 + random.nextInt(3); i++) {
            demoTransactions.add(Transaction.builder()
                    .description("Grocery Store")
                    .amount(BigDecimal.valueOf(40 + random.nextInt(60)))
                    .date(LocalDate.of(year, mon, 1 + random.nextInt(27)))
                    .category(categories.get("Groceries"))
                    .type(categories.get("Groceries").getType())
                    .user(user)
                    .build());
          }

          for (int i = 0; i < 2 + random.nextInt(3); i++) {
            demoTransactions.add(Transaction.builder()
                    .description("Restaurant")
                    .amount(BigDecimal.valueOf(20 + random.nextInt(30)))
                    .date(LocalDate.of(year, mon, 1 + random.nextInt(27)))
                    .category(categories.get("Dining"))
                    .type(categories.get("Dining").getType())
                    .user(user)
                    .build());
          }

          demoTransactions.add(Transaction.builder()
                  .description("Electric Bill")
                  .amount(BigDecimal.valueOf(70 + random.nextInt(100)))
                  .date(LocalDate.of(year, mon, 10))
                  .category(categories.get("Utilities"))
                  .type(categories.get("Utilities").getType())
                  .user(user)
                  .build());

          if (random.nextBoolean()) {
            demoTransactions.add(Transaction.builder()
                    .description("Movie Theater")
                    .amount(BigDecimal.valueOf(30 + random.nextInt(20)))
                    .date(LocalDate.of(year, mon, 1 + random.nextInt(27)))
                    .category(categories.get("Entertainment"))
                    .type(categories.get("Entertainment").getType())
                    .user(user)
                    .build());
          }

          if (mon >= 6 && mon <= 8) {
            demoTransactions.add(Transaction.builder()
                    .description("Weekend Trip")
                    .amount(BigDecimal.valueOf(200 + random.nextInt(300)))
                    .date(LocalDate.of(year, mon, 1 + random.nextInt(27)))
                    .category(categories.get("Travel"))
                    .type(categories.get("Travel").getType())
                    .user(user)
                    .build());
          }

          if (mon == 2 || mon == 12) {
            demoTransactions.add(Transaction.builder()
                    .description("Gift Purchase")
                    .amount(BigDecimal.valueOf(25 + random.nextInt(75)))
                    .date(LocalDate.of(year, mon, 1 + random.nextInt(27)))
                    .category(categories.get("Gifts"))
                    .type(categories.get("Gifts").getType())
                    .user(user)
                    .build());
          }
        }

        transactionRepository.saveAll(demoTransactions);

      };
    }
  }
