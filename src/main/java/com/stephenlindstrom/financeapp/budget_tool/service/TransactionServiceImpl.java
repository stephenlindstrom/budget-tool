package com.stephenlindstrom.financeapp.budget_tool.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.stephenlindstrom.financeapp.budget_tool.dto.TransactionFilter;
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
  public List<Transaction> filter(TransactionFilter filter) {
    List<Transaction> result = transactionRepository.findAll();

    if (filter.getType() != null) {
      result = result.stream()
              .filter(t -> t.getType() == filter.getType())
              .toList();
    }

    if (filter.getCategoryId() != null) {
      result = result.stream()
              .filter(t -> t.getCategory() != null && t.getCategory().getId().equals(filter.getCategoryId()))
              .toList();
    }

    if (filter.getStartDate() != null) {
      result = result.stream()
              .filter(t -> !t.getDate().isBefore(filter.getStartDate()))
              .toList();
    }

    if (filter.getEndDate() != null) {
      result = result.stream()
              .filter(t -> !t.getDate().isAfter(filter.getEndDate()))
              .toList();
    }

    return result;
  }

  @Override
  public void deleteById(Long id) {
    transactionRepository.deleteById(id);
  }
}
