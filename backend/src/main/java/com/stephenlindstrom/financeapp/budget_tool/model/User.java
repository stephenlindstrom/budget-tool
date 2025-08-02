package com.stephenlindstrom.financeapp.budget_tool.model;

import java.util.List;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String username;

  @Column(nullable = false)
  private String password;

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
  private List<Budget> budgets;

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
  private List<Transaction> transactions;

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
  private List<Category> categories;
}
