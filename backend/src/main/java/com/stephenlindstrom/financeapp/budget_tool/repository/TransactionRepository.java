package com.stephenlindstrom.financeapp.budget_tool.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.stephenlindstrom.financeapp.budget_tool.enums.TransactionType;
import com.stephenlindstrom.financeapp.budget_tool.model.Transaction;
import com.stephenlindstrom.financeapp.budget_tool.model.User;


@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
  interface SpentByCategoryRow {
    Long getCategoryId();
    String getCategoryName();
    BigDecimal getSpent();
  }

  @Query("""
    select t.category.id as categoryId,
            t.category.name as categoryName,
            sum(t.amount) as spent
    from Transaction t
    where t.user = :user
      and t.type = :expenseType
      and t.date >= :startInclusive
      and t.date < :endExclusive
    group by t.category.id
  """)
  List<SpentByCategoryRow> findMonthlySpentByCategory(
      @Param("user") User user,
      @Param("startInclusive") LocalDate startInclusive,
      @Param("endExclusive") LocalDate endExclusive,
      @Param("expenseType") TransactionType expenseType
  );

  interface MonthlySpentRow {
    Integer getYear();
    Integer getMonth();
    BigDecimal getSpent();
  }

  @Query("""
  select function('YEAR', t.date)  as year,
         function('MONTH', t.date) as month,
         sum(t.amount) as spent
  from Transaction t
  where t.user = :user
    and t.type = :expenseType
  group by function('YEAR', t.date), function('MONTH', t.date)
  order by year desc, month desc
  """)
  List<MonthlySpentRow> findMonthlySpentByMonth(
      @Param("user") User user,
      @Param("expenseType") TransactionType expenseType
  );

  List<Transaction> findByType(TransactionType type);

  List<Transaction> findByCategoryId(Long categoryId);

  List<Transaction> findByDateBetween(LocalDate start, LocalDate end);

  List<Transaction> findByUserOrderByDateDesc(User user);

  Optional<Transaction> findByIdAndUser(Long id, User user);

  void deleteByIdAndUser(Long id, User user);
}
