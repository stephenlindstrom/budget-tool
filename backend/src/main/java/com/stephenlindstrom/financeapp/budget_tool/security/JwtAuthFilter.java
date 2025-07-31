package com.stephenlindstrom.financeapp.budget_tool.security;

import java.io.IOException;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.stephenlindstrom.financeapp.budget_tool.service.JwtService;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

  private final JwtService jwtService;
  private final UserDetailsService userDetailsService;
  private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

  public JwtAuthFilter(JwtService jwtService, UserDetailsService userDetailsService, JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint) {
    this.jwtService = jwtService;
    this.userDetailsService = userDetailsService;
    this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response,
                                  FilterChain filterChain)
    throws ServletException, IOException {
      final String authHeader = request.getHeader("Authorization");
      final String jwt;
      final String username;

      if (authHeader == null ) {
        filterChain.doFilter(request, response);
        return;
      }

      String trimmedHeader = authHeader.trim();

      if (!trimmedHeader.startsWith("Bearer ")) {
        filterChain.doFilter(request, response);
        return;
      }

      jwt = trimmedHeader.substring(7);
      
      try {
        username = jwtService.extractUsername(jwt);
      } catch (JwtException | IllegalArgumentException e) {
          jwtAuthenticationEntryPoint.commence(request, response, 
            new BadCredentialsException("Invalid or expired JWT token", e));
          return;
      }
      

      if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        if (jwtService.isTokenValid(jwt, userDetails.getUsername())) {
          UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
            userDetails,
            null,
            userDetails.getAuthorities()
          );

          authToken.setDetails(
            new WebAuthenticationDetailsSource().buildDetails(request)
          );

          SecurityContextHolder.getContext().setAuthentication(authToken);
        }
      }

      filterChain.doFilter(request, response);
    }
}
