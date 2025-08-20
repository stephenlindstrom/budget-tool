package com.stephenlindstrom.financeapp.budget_tool.errors;

public class BadRequestException extends RuntimeException {
  public BadRequestException(String message) {
    super(message);
  }
}
