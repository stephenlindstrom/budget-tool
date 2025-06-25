package com.stephenlindstrom.financeapp.budget_tool.service;

import java.util.List;

import com.stephenlindstrom.financeapp.budget_tool.dto.TransactionCreateDTO;
import com.stephenlindstrom.financeapp.budget_tool.dto.TransactionDTO;
import com.stephenlindstrom.financeapp.budget_tool.dto.TransactionFilter;

public interface TransactionService {
  TransactionDTO save(TransactionCreateDTO dto);
  List<TransactionDTO> getAll();
  List<TransactionDTO> filter(TransactionFilter filter);
  TransactionDTO updateById(Long id, TransactionCreateDTO dto);
  void deleteById(Long id);
}
