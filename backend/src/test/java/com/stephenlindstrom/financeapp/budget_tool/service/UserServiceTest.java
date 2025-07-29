package com.stephenlindstrom.financeapp.budget_tool.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.stephenlindstrom.financeapp.budget_tool.dto.UserRegistrationDTO;
import com.stephenlindstrom.financeapp.budget_tool.model.User;
import com.stephenlindstrom.financeapp.budget_tool.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @InjectMocks
  private UserService userService;

  @Captor
  private ArgumentCaptor<User> userCaptor;

  @Test
  void testRegisterUser_validRegistration_createsUser() {

    UserRegistrationDTO dto = UserRegistrationDTO.builder()
                                .username("testuser")
                                .password("rawPassword")
                                .build();

    when(userRepository.findByUsername(dto.getUsername())).thenReturn(Optional.empty());
    when(passwordEncoder.encode(dto.getPassword())).thenReturn("encodedPassword");

    userService.registerUser(dto);

    verify(userRepository).save(userCaptor.capture());
    User savedUser = userCaptor.getValue();

    assertEquals(dto.getUsername(), savedUser.getUsername());
    assertEquals("encodedPassword", savedUser.getPassword());
  }

  @Test
  void testRegisterUser_usernameAlreadyExists_throwsIllegalArgumentException() {
    UserRegistrationDTO dto = UserRegistrationDTO.builder()
                                .username("alreadyExists")
                                .password("rawPassword")
                                .build();

    when(userRepository.findByUsername(dto.getUsername())).thenReturn(Optional.of(new User()));

    assertThrows(IllegalArgumentException.class, () -> {
      userService.registerUser(dto);
    });

    verify(userRepository, never()).save(any());
  }
  
  @Test
  void testAuthenticateUser_whenCredentialsValid_returnsUser() {
    User user = User.builder()
                .username("testuser")
                .password("hashedPassword")
                .build();

    when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("rawPassword", user.getPassword())).thenReturn(true);

    User authenticatedUser = userService.authenticateUser("testuser", "rawPassword");

    assertEquals(user.getUsername(), authenticatedUser.getUsername());
    assertEquals(user.getPassword(), authenticatedUser.getPassword());
    verify(passwordEncoder).matches("rawPassword", "hashedPassword");
  }

  @Test
  void testAuthenticateUser_whenUsernameInvalid_throwsIllegalArgumentException() {
    when(userRepository.findByUsername("invalidUsername")).thenReturn(Optional.empty());

    assertThrows(IllegalArgumentException.class, () -> {
      userService.authenticateUser("invalidUsername", "rawPassword");
    });
  }

  @Test
  void testAuthenticateUser_whenPasswordInvalid_throwsIllegalArgumentException() {
    User user = User.builder()
                .username("testuser")
                .password("hashedPassword")
                .build();

    when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("rawPassword", user.getPassword())).thenReturn(false);

    assertThrows(IllegalArgumentException.class, () -> {
      userService.authenticateUser("testuser", "rawPassword");
    });
    verify(passwordEncoder).matches("rawPassword", user.getPassword());
  }

}
