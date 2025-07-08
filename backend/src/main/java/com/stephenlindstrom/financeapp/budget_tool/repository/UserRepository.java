package com.stephenlindstrom.financeapp.budget_tool.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.stephenlindstrom.financeapp.budget_tool.model.User;

public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByUsername(String username);

}
