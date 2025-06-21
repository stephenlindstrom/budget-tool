package com.stephenlindstrom.financeapp.budget_tool.errors;

public class ResourceNotFoundException extends RuntimeException {
  public ResourceNotFoundException(String message) {
    super(message);
  }
}
