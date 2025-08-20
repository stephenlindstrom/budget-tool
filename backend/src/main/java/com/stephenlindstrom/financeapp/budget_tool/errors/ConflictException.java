package com.stephenlindstrom.financeapp.budget_tool.errors;

public class ConflictException extends RuntimeException {
  public ConflictException(String message) {
    super(message);
  }
}
