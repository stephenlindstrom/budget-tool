package com.stephenlindstrom.financeapp.budget_tool.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.stephenlindstrom.financeapp.budget_tool.dto.CategoryDTO;
import com.stephenlindstrom.financeapp.budget_tool.dto.TransactionCreateDTO;
import com.stephenlindstrom.financeapp.budget_tool.dto.TransactionDTO;
import com.stephenlindstrom.financeapp.budget_tool.dto.TransactionFilter;
import com.stephenlindstrom.financeapp.budget_tool.errors.ResourceNotFoundException;
import com.stephenlindstrom.financeapp.budget_tool.model.Category;
import com.stephenlindstrom.financeapp.budget_tool.model.Transaction;
import com.stephenlindstrom.financeapp.budget_tool.repository.CategoryRepository;
import com.stephenlindstrom.financeapp.budget_tool.repository.TransactionRepository;

@Service
public class TransactionServiceImpl implements TransactionService {

  private final TransactionRepository transactionRepository;
  private final CategoryRepository categoryRepository;

  public TransactionServiceImpl(TransactionRepository transactionRepository, CategoryRepository categoryRepository) {
    this.transactionRepository = transactionRepository;
    this.categoryRepository = categoryRepository;
  }

  @Override
  public TransactionDTO save(TransactionCreateDTO dto) {
    Transaction transaction = mapToEntity(dto);
    Transaction saved = transactionRepository.save(transaction);
    return mapToDTO(saved);
  }

  @Override
  public List<TransactionDTO> getAll() {
    return transactionRepository.findAll(Sort.by(Sort.Direction.DESC, "date")).stream().map(this::mapToDTO).toList();
  } 

  @Override
  public List<TransactionDTO> filter(TransactionFilter filter) {
    List<Transaction> result = transactionRepository.findAll();

    if (filter.getType() != null) {
      result = result.stream()
              .filter(t -> t.getType() == filter.getType())
              .toList();
    }

    if (filter.getCategoryId() != null) {
      result = result.stream()
              .filter(t -> t.getCategory() != null && t.getCategory().getId().equals(filter.getCategoryId()))
              .toList();
    }

    if (filter.getStartDate() != null) {
      result = result.stream()
              .filter(t -> !t.getDate().isBefore(filter.getStartDate()))
              .toList();
    }

    if (filter.getEndDate() != null) {
      result = result.stream()
              .filter(t -> !t.getDate().isAfter(filter.getEndDate()))
              .toList();
    }

    return result.stream().map(this::mapToDTO).toList();
  }

  @Override
  public void deleteById(Long id) {
    transactionRepository.deleteById(id);
  }

  private TransactionDTO mapToDTO(Transaction transaction) {
    Category category = transaction.getCategory();

    CategoryDTO categoryDTO = CategoryDTO.builder()
                              .id(category.getId())
                              .name(category.getName())
                              .type(category.getType())
                              .build();

    return TransactionDTO.builder()
            .id(transaction.getId())
            .amount(transaction.getAmount())
            .category(categoryDTO)
            .type(transaction.getType())
            .date(transaction.getDate())
            .description(transaction.getDescription())
            .build();
  }
  
  private Transaction mapToEntity(TransactionCreateDTO dto) {
    Category category = categoryRepository.findById(dto.getCategoryId())
      .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

    return Transaction.builder()
            .amount(dto.getAmount())
            .category(category)
            .type(dto.getType())
            .date(dto.getDate() != null  ? dto.getDate() : LocalDate.now())
            .description(dto.getDescription())
            .build();
  }

}
