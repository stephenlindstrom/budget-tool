package com.stephenlindstrom.financeapp.budget_tool.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.stephenlindstrom.financeapp.budget_tool.model.User;
import com.stephenlindstrom.financeapp.budget_tool.repository.UserRepository;
import com.stephenlindstrom.financeapp.budget_tool.service.JwtService;

@Configuration
@Profile("demo")
public class DataInitializer {

  @Bean
  public CommandLineRunner preloadUser(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
    return args -> {
      String username = "demoUser";
      String password = "demoPassword";

      if (userRepository.findByUsername(username).isEmpty()) {
        User user = User.builder()
                    .username(username)
                    .password(passwordEncoder.encode(password))
                    .build();
        
        userRepository.save(user);

        String jwt = jwtService.generateToken(username);
        System.out.println("Preloaded demo user: " + username + " / " + password);
        System.out.println("Demo JWT token: Bearer " + jwt);
      }
    };
  }
}
