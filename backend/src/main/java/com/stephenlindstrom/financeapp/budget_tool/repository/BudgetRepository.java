package com.stephenlindstrom.financeapp.budget_tool.repository;

import java.time.YearMonth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.stephenlindstrom.financeapp.budget_tool.model.Budget;
import com.stephenlindstrom.financeapp.budget_tool.model.User;

import java.util.List;
import java.util.Optional;


@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {
  
  boolean existsByCategoryIdAndMonthAndUser(Long categoryId, YearMonth month, User user);
  
  List<Budget> findByMonthAndUser(YearMonth month, User user);

  @Query("SELECT DISTINCT b.month FROM Budget b WHERE b.user = :user ORDER BY b.month DESC")
  List<YearMonth> findDistinctMonthsByUser(@Param("user") User user);

  List<Budget> findByUserOrderByMonthDesc(User user);

  Optional<Budget> findByIdAndUser(Long id, User user);

  void deleteByIdAndUser(Long id, User user);

}
