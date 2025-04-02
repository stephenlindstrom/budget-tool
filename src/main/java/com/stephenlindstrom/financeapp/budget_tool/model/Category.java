package com.stephenlindstrom.financeapp.budget_tool.model;

import java.util.List;

import com.stephenlindstrom.financeapp.budget_tool.enums.TransactionType;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String name;

  @Enumerated(EnumType.STRING)
  private TransactionType type;

  @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
  private List<Transaction> transactions;

}
