package com.stephenlindstrom.financeapp.budget_tool.errors;

public class CategoryTypeMismatchException extends RuntimeException {
  public CategoryTypeMismatchException(String message) {
    super(message);
  }
}
