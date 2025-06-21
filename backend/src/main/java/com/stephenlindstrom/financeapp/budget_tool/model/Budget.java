package com.stephenlindstrom.financeapp.budget_tool.model;

import java.math.BigDecimal;
import java.time.YearMonth;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.stephenlindstrom.financeapp.budget_tool.converter.YearMonthConverter;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Budget {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "budget_value")
  private BigDecimal value;

  @Convert(converter = YearMonthConverter.class)
  @Column(name = "budget_month")
  private YearMonth month;

  @JsonIgnore
  @ToString.Exclude
  @ManyToOne
  @JoinColumn(name = "category_id")
  private Category category;

}
