package com.stephenlindstrom.financeapp.budget_tool.service;

import java.time.LocalDate;
import java.util.List;

import com.stephenlindstrom.financeapp.budget_tool.enums.TransactionType;
import com.stephenlindstrom.financeapp.budget_tool.model.Transaction;

public interface TransactionService {
  Transaction save(Transaction transaction);
  List<Transaction> getAll();
  List<Transaction> getByType(TransactionType type);
  List<Transaction> getByCategory(String category);
  List<Transaction> getByDateRange(LocalDate start, LocalDate end);
  void deleteById(Long id);
}
