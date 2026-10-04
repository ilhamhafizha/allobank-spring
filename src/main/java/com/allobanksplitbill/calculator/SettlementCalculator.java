package com.allobanksplitbill.calculator;


import com.allobanksplitbill.dto.respon.SettlementResponse;
import com.allobanksplitbill.entity.Expense;
import com.allobanksplitbill.entity.ExpenseParticipant;
import com.allobanksplitbill.entity.Participant;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class SettlementCalculator {

    public List<SettlementResponse.SettlementTransaction> calculate(
            List<Participant> participants,
            List<Expense> expenses,
            BigDecimal serviceChargePct
    ) {
        Map<Long, BigDecimal> balances = new LinkedHashMap<>();
        Map<Long, Participant> participantById = new LinkedHashMap<>();

        for (Participant participant : participants) {
            balances.put(participant.getId(), BigDecimal.ZERO);
            participantById.put(participant.getId(), participant);
        }

        for (Expense expense : expenses) {
            List<Participant> sharedWith = expense.getParticipants()
                    .stream()
                    .map(ExpenseParticipant::getParticipant)
                    .toList();

            if (sharedWith.isEmpty()) {
                throw new IllegalArgumentException(
                        "An expense must have at least one participant"
                );
            }

            BigDecimal amount = expense.getAmount();

            BigDecimal charge = amount
                    .multiply(serviceChargePct)
                    .divide(
                            BigDecimal.valueOf(100),
                            2,
                            RoundingMode.HALF_UP
                    );

            BigDecimal totalWithCharge = amount.add(charge);

            Long payerId = expense.getPaidBy().getId();

            balances.merge(
                    payerId,
                    totalWithCharge,
                    BigDecimal::add
            );

            BigDecimal share = totalWithCharge.divide(
                    BigDecimal.valueOf(sharedWith.size()),
                    2,
                    RoundingMode.DOWN
            );

            BigDecimal allocated = BigDecimal.ZERO;

            for (int i = 0; i < sharedWith.size(); i++) {
                Participant participant = sharedWith.get(i);

                BigDecimal participantShare;

                if (i == sharedWith.size() - 1) {
                    participantShare = totalWithCharge.subtract(allocated);
                } else {
                    participantShare = share;
                }

                allocated = allocated.add(participantShare);

                balances.merge(
                        participant.getId(),
                        participantShare.negate(),
                        BigDecimal::add
                );
            }
        }

        List<Map.Entry<Long, BigDecimal>> debtors =
                new ArrayList<>();

        List<Map.Entry<Long, BigDecimal>> creditors =
                new ArrayList<>();

        for (Map.Entry<Long, BigDecimal> entry : balances.entrySet()) {
            int comparison = entry.getValue().compareTo(BigDecimal.ZERO);

            if (comparison < 0) {
                debtors.add(entry);
            } else if (comparison > 0) {
                creditors.add(entry);
            }
        }

        List<SettlementResponse.SettlementTransaction> transactions =
                new ArrayList<>();

        int debtorIndex = 0;
        int creditorIndex = 0;

        while (debtorIndex < debtors.size()
                && creditorIndex < creditors.size()) {

            Map.Entry<Long, BigDecimal> debtor =
                    debtors.get(debtorIndex);

            Map.Entry<Long, BigDecimal> creditor =
                    creditors.get(creditorIndex);

            BigDecimal debt = debtor.getValue().abs();
            BigDecimal credit = creditor.getValue();

            BigDecimal transfer = debt.min(credit);

            Participant from = participantById.get(debtor.getKey());
            Participant to = participantById.get(creditor.getKey());

            transactions.add(
                    new SettlementResponse.SettlementTransaction(
                            from.getName(),
                            to.getName(),
                            transfer.setScale(2, RoundingMode.HALF_UP)
                    )
            );

            debtor.setValue(debtor.getValue().add(transfer));
            creditor.setValue(creditor.getValue().subtract(transfer));

            if (debtor.getValue().compareTo(BigDecimal.ZERO) == 0) {
                debtorIndex++;
            }

            if (creditor.getValue().compareTo(BigDecimal.ZERO) == 0) {
                creditorIndex++;
            }
        }

        return transactions;
    }
}

