package com.allobanksplitbill.service;

import com.allobanksplitbill.calculator.SettlementCalculator;
import com.allobanksplitbill.entity.Expense;
import com.allobanksplitbill.entity.ExpenseParticipant;
import com.allobanksplitbill.entity.Participant;
import com.allobanksplitbill.repository.BillGroupRepository;
import com.allobanksplitbill.repository.ExpenseRepository;
import com.allobanksplitbill.repository.ParticipantRepository;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SettlementServiceTest {
    private final BillGroupRepository groups = mock(BillGroupRepository.class);
    private final ParticipantRepository participants = mock(ParticipantRepository.class);
    private final ExpenseRepository expenses = mock(ExpenseRepository.class);
    private final SettlementService service = new SettlementService(groups, participants, expenses,
            new SettlementCalculator(), "ILHAMHAFIZHA");

    @Test
    void shouldComputePersonalizationAndReconcileSummaryWithTransfers() {
        var payer = new Participant();
        payer.setId(1L);
        payer.setName("Payer");
        var beneficiary = new Participant();
        beneficiary.setId(2L);
        beneficiary.setName("Beneficiary");
        when(groups.existsById(1L)).thenReturn(true);
        when(participants.findAllByGroupId(1L)).thenReturn(List.of(payer, beneficiary));
        when(expenses.findAllByGroupId(1L)).thenReturn(List.of(
                expense(1L, payer, beneficiary), expense(2L, payer, beneficiary)));

        var result = service.getSettlement(1L);
        assertEquals(new BigDecimal("4"), result.getServiceChargePct());
        assertEquals(new BigDecimal("0.26"), result.getTotalExpense());
        assertEquals(new BigDecimal("0.01"), result.getServiceChargeAmount());
        assertEquals(result.getTotalExpense().add(result.getServiceChargeAmount()),
                result.getTransactions().get(0).getAmount());
    }

    @Test
    void shouldReturn404ForMissingGroup() {
        var error = assertThrows(ResponseStatusException.class, () -> service.getSettlement(99L));
        assertEquals(404, error.getStatusCode().value());
        verifyNoInteractions(participants, expenses);
    }

    private Expense expense(Long id, Participant payer, Participant beneficiary) {
        var expense = new Expense();
        expense.setId(id);
        expense.setAmount(new BigDecimal("0.13"));
        expense.setPaidBy(payer);
        var share = new ExpenseParticipant();
        share.setExpense(expense);
        share.setParticipant(beneficiary);
        expense.setParticipants(List.of(share));
        return expense;
    }
}
