package com.stephenlindstrom.financeapp.budget_tool.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.stephenlindstrom.financeapp.budget_tool.model.User;
import com.stephenlindstrom.financeapp.budget_tool.repository.UserRepository;

@Service
public class UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  public User registerUser(User user) {
    // Check if username already exists
    if (userRepository.findByUsername(user.getUsername()).isPresent()) {
      throw new IllegalArgumentException("Username already taken.");
    }

    // Hash the password before saving
    String encodedPassword = passwordEncoder.encode(user.getPassword());
    user.setPassword(encodedPassword);

    return userRepository.save(user);
  }

  public User authenticateUser(String username, String rawPassword) {
    User user = userRepository.findByUsername(username)
      .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));

    if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
      throw new IllegalArgumentException("Invalid username or password");
    }

    return user;
  }
}
