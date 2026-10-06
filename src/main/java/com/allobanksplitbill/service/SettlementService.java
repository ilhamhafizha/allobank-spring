package com.allobanksplitbill.service;



import com.allobanksplitbill.calculator.SettlementCalculator;
import com.allobanksplitbill.dto.respon.SettlementResponse;
import com.allobanksplitbill.entity.Expense;
import com.allobanksplitbill.entity.Participant;
import com.allobanksplitbill.repository.BillGroupRepository;
import com.allobanksplitbill.repository.ExpenseRepository;
import com.allobanksplitbill.repository.ParticipantRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class SettlementService {

    private final BillGroupRepository billGroupRepository;
    private final ParticipantRepository participantRepository;
    private final ExpenseRepository expenseRepository;
    private final SettlementCalculator settlementCalculator;
    private final String githubUsername;

    public SettlementService(
            BillGroupRepository billGroupRepository,
            ParticipantRepository participantRepository,
            ExpenseRepository expenseRepository,
            SettlementCalculator settlementCalculator,
            @Value("${app.github-username}") String githubUsername
    ) {
        this.billGroupRepository = billGroupRepository;
        this.participantRepository = participantRepository;
        this.expenseRepository = expenseRepository;
        this.settlementCalculator = settlementCalculator;
        this.githubUsername = githubUsername;
    }

    @Transactional(readOnly = true)
    public SettlementResponse getSettlement(Long groupId) {
        if (!billGroupRepository.existsById(groupId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Bill group not found"
            );
        }

        List<Participant> participants =
                participantRepository.findAllByGroupId(groupId);

        List<Expense> expenses =
                expenseRepository.findAllByGroupId(groupId);

        BigDecimal totalExpense = expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal serviceChargePct = calculateServiceChargePct();

        BigDecimal serviceChargeAmount = settlementCalculator.calculateServiceChargeAmount(
                totalExpense, serviceChargePct);

        var transactions = settlementCalculator.calculate(
                participants,
                expenses,
                serviceChargePct
        );

        return new SettlementResponse(
                groupId,
                totalExpense,
                serviceChargePct,
                serviceChargeAmount,
                transactions
        );
    }

    private BigDecimal calculateServiceChargePct() {
        int characterSum = githubUsername
                .toLowerCase(java.util.Locale.ROOT)
                .chars()
                .sum();

        return BigDecimal.valueOf(characterSum % 10);
    }
}

