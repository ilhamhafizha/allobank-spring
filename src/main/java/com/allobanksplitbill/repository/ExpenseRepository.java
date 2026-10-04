package com.allobanksplitbill.repository;

import com.allobanksplitbill.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findAllByGroupId(Long groupId);
}