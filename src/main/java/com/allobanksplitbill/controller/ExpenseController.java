package com.allobanksplitbill.controller;

import com.allobanksplitbill.dto.request.CreateExpenseRequest;
import com.allobanksplitbill.dto.respon.ExpenseResponse;
import com.allobanksplitbill.service.ExpenseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bill-groups/{groupId}/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExpenseResponse createExpense(
            @PathVariable Long groupId,
            @Valid @RequestBody CreateExpenseRequest request) {
        return expenseService.createExpense(groupId, request);
    }
}
