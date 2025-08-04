package com.stephenlindstrom.financeapp.budget_tool.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.stephenlindstrom.financeapp.budget_tool.enums.TransactionType;
import com.stephenlindstrom.financeapp.budget_tool.model.Transaction;
import com.stephenlindstrom.financeapp.budget_tool.model.User;


@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

  List<Transaction> findByType(TransactionType type);

  List<Transaction> findByCategoryId(Long categoryId);

  List<Transaction> findByDateBetween(LocalDate start, LocalDate end);

  List<Transaction> findByUserOrderByDateDesc(User user);

  Optional<Transaction> findByIdAndUser(Long id, User user);

  void deleteByIdAndUser(Long id, User user);
}
