package com.stephenlindstrom.financeapp.budget_tool.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.stephenlindstrom.financeapp.budget_tool.model.Category;
import com.stephenlindstrom.financeapp.budget_tool.model.User;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
  boolean existsByNameIgnoreCaseAndUser(String name, User user);

  Optional<Category> findByIdAndUser(Long id, User user);

  List<Category> findByUserOrderByName(User user);

  void deleteByIdAndUser(Long id, User user);

}
