package com.stephenlindstrom.financeapp.budget_tool.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.stephenlindstrom.financeapp.budget_tool.model.TestEntity;

@Repository
public interface TestRepository extends JpaRepository<TestEntity, Long> {

}
