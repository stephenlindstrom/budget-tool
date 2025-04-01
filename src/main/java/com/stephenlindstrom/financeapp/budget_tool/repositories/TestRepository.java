package com.stephenlindstrom.financeapp.budget_tool.repositories;

import com.stephenlindstrom.financeapp.budget_tool.model.TestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TestRepository extends JpaRepository<TestEntity, Long> {

}
