package com.stephenlindstrom.financeapp.budget_tool.service;

import java.util.List;

import com.stephenlindstrom.financeapp.budget_tool.dto.TransactionFilter;
import com.stephenlindstrom.financeapp.budget_tool.model.Transaction;

public interface TransactionService {
  Transaction save(Transaction transaction);
  List<Transaction> getAll();
  List<Transaction> filter(TransactionFilter filter);
  void deleteById(Long id);
}
