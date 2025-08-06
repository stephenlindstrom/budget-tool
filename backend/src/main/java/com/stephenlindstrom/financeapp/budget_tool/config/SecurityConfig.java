package com.stephenlindstrom.financeapp.budget_tool.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.security.config.Customizer.withDefaults;

import com.stephenlindstrom.financeapp.budget_tool.security.JwtAuthFilter;
import com.stephenlindstrom.financeapp.budget_tool.security.JwtAuthenticationEntryPoint;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  // Custom filter that intercepts requests to validate JWTs
  private final JwtAuthFilter jwtAuthFilter;

  // Custom entry point that handles unauthorized access (returns 401)
  private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

  public SecurityConfig(JwtAuthFilter jwtAuthFilter, JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint) {
    this.jwtAuthFilter = jwtAuthFilter;
    this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
  }

  /**
   * Configures which endpoints are public, how exceptions are handled, 
   * and where to place the JWT filter.
   */
  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
      .cors(withDefaults())
      .csrf(csrf -> csrf.disable())
      .exceptionHandling(ex -> ex
        .authenticationEntryPoint(jwtAuthenticationEntryPoint))
      .authorizeHttpRequests(auth -> auth
        .requestMatchers(
          "/api/auth/**",
          "/v3/api-docs/**",
          "/swagger-ui/**").permitAll()
        .anyRequest().authenticated()
      )
      .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
