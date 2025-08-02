package com.stephenlindstrom.financeapp.budget_tool.service;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.stephenlindstrom.financeapp.budget_tool.dto.UserRegistrationDTO;
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

  /**
   * Registers a new user by checking for duplicates and encoding the password.
   * 
   * @param dto The DTO containing username and raw password
   * @throws IllegalArgumentException if the username is already taken
   */
  public void registerUser(UserRegistrationDTO dto) {
    // Check if username already exists
    if (userRepository.findByUsername(dto.getUsername()).isPresent()) {
      throw new IllegalArgumentException("Username already taken.");
    }

    // Hash the password before saving
    String encodedPassword = passwordEncoder.encode(dto.getPassword());
    
    User user = mapToEntity(dto, encodedPassword);
    userRepository.save(user);
  }

  /**
   * Authenticates a user by checking if the username exists and the password matches.
   * 
   * @param username the input username
   * @param rawPassword the unencrypted password provided by the user
   * @return the authenticated User entity
   * @throws BadCredentialsException if the user is not found or the password doesn't match
   */
  public User authenticateUser(String username, String rawPassword) {
    User user = userRepository.findByUsername(username)
      .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));

    if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
      throw new BadCredentialsException("Invalid username or password");
    }

    return user;
  }

  /**
   * Retrieves authenticated user or else throws UsernameNotFoundException.
   * 
   * @return authenticated User entity
   * @throws UsernameNotFoundException if the authenticated user is not found
   */
  public User getAuthenticatedUser() {
    String username = SecurityContextHolder.getContext().getAuthentication().getName();
    return userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
  }

  /**
   * Helper method to map registration DTO and encoded password to a User entity.
   * 
   * @param dto the registration DTO
   * @param encodedPassword the hashed password
   * @return a new User entity
   */
  private User mapToEntity(UserRegistrationDTO dto, String encodedPassword) {
    return User.builder()
            .username(dto.getUsername())
            .password(encodedPassword)
            .build();
  }
}
