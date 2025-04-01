package com.stephenlindstrom.financeapp.budget_tool.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import com.stephenlindstrom.financeapp.budget_tool.enums.TransactionType;
import com.stephenlindstrom.financeapp.budget_tool.model.Transaction;


@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

  List<Transaction> findByType(TransactionType type);

  List<Transaction> findByCategory(String category);

  List<Transaction> findByDateBetween(LocalDate start, LocalDate end);
}
