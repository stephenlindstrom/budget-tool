package com.stephenlindstrom.financeapp.budget_tool.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.stephenlindstrom.financeapp.budget_tool.model.User;
import com.stephenlindstrom.financeapp.budget_tool.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class UserDetailsServiceImplTest {

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private UserDetailsServiceImpl userDetailsService;

  @Test
   void testLoadUserByUsername_userExists_returnsUserDetails() {
    User user = User.builder()
                  .username("testuser")
                  .password("hashedpass")
                  .build();

    when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

    UserDetails userDetails = userDetailsService.loadUserByUsername("testuser");

    assertEquals("testuser", userDetails.getUsername());
    assertEquals("hashedpass", userDetails.getPassword());
   }

   @Test
   void testLoadUserByUsername_userDoesNotExist_throwsUsernameNotFoundException() {
    when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());

    assertThrows(UsernameNotFoundException.class, () -> {
      userDetailsService.loadUserByUsername("missing");
    });
   }
}
