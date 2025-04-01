package com.stephenlindstrom.financeapp.budget_tool.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.stephenlindstrom.financeapp.budget_tool.enums.TransactionType;
import com.stephenlindstrom.financeapp.budget_tool.model.Transaction;
import com.stephenlindstrom.financeapp.budget_tool.repository.TransactionRepository;

@Service
public class TransactionServiceImpl implements TransactionService {

  private final TransactionRepository transactionRepository;

  public TransactionServiceImpl(TransactionRepository transactionRepository) {
    this.transactionRepository = transactionRepository;
  }

  @Override
  public Transaction save(Transaction transaction) {
    return transactionRepository.save(transaction);
  }

  @Override
  public List<Transaction> getAll() {
    return transactionRepository.findAll();
  } 

  @Override
  public List<Transaction> getByType(TransactionType type) {
    return transactionRepository.findByType(type);
  }

  @Override
  public List<Transaction> getByCategory(String category) {
    return transactionRepository.findByCategory(category);
  }

  @Override
  public List<Transaction> getByDateRange(LocalDate start, LocalDate end) {
    return transactionRepository.findByDateBetween(start, end);
  }

  @Override
  public void deleteById(Long id) {
    transactionRepository.deleteById(id);
  }
}
