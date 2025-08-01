package com.stephenlindstrom.financeapp.budget_tool.service;

import java.util.List;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.stephenlindstrom.financeapp.budget_tool.model.User;
import com.stephenlindstrom.financeapp.budget_tool.repository.UserRepository;
/**
 * Custom implementation of Spring Security's UserDetailsService interface.
 * Used by Spring Security to retrieve user details during authentication.
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

  private final UserRepository userRepository;

  public UserDetailsServiceImpl(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    User user = userRepository.findByUsername(username)
        .orElseThrow(() -> new UsernameNotFoundException("User not found"));

    // Return a Spring Security-compatible User object
    return new org.springframework.security.core.userdetails.User(
      user.getUsername(),
      user.getPassword(),
      List.of(new SimpleGrantedAuthority("ROLE_USER"))
    );
  }
}
