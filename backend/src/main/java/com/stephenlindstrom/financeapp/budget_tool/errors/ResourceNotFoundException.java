package com.stephenlindstrom.financeapp.budget_tool.errors;

/**
 * Exception thrown when a requested resource is not found in the database.
 * Typically used in service layer methods to trigger a 404 response.
 */
public class ResourceNotFoundException extends RuntimeException {
  public ResourceNotFoundException(String message) {
    super(message);
  }
}
