package com.stephenlindstrom.financeapp.budget_tool.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.stephenlindstrom.financeapp.budget_tool.models.Transaction;

import java.time.LocalDate;
import java.util.List;
import com.stephenlindstrom.financeapp.budget_tool.enums.TransactionType;


@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

  List<Transaction> findByType(TransactionType type);

  List<Transaction> findByCategory(String category);
  
  List<Transaction> findByDateBetween(LocalDate start, LocalDate end);
}
