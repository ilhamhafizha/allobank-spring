package com.allobanksplitbill.repository;

import com.allobanksplitbill.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    @EntityGraph(attributePaths = {"paidBy", "participants", "participants.participant"})
    List<Expense> findAllByGroupId(Long groupId);
}