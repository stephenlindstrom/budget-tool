package com.stephenlindstrom.financeapp.budget_tool.models;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.stephenlindstrom.financeapp.budget_tool.enums.TransactionType;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private BigDecimal amount;
  
  private String category;

  @Enumerated(EnumType.STRING)
  private TransactionType type;

  private LocalDate date;

  private String description;
}
