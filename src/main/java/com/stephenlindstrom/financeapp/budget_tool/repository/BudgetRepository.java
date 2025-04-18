package com.stephenlindstrom.financeapp.budget_tool.repository;

import java.time.YearMonth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.stephenlindstrom.financeapp.budget_tool.model.Budget;
import java.util.List;


@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {
  
  boolean existsByCategoryIdAndMonth(Long categoryId, YearMonth month);
  
  List<Budget> findByMonth(YearMonth month);

  @Query("SELECT DISTINCT b.month FROM Budget b ORDER BY b.month DESC")
  List<YearMonth> findDistinctMonths();
}
