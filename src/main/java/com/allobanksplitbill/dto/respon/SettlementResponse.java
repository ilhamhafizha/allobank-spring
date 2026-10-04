package com.allobanksplitbill.dto.respon;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@AllArgsConstructor
public class SettlementResponse {

    private Long groupId;
    private BigDecimal totalExpense;
    private BigDecimal serviceChargePct;
    private BigDecimal serviceChargeAmount;
    private List<SettlementTransaction> transactions;

    @Getter
    @AllArgsConstructor
    public static class SettlementTransaction {

        private String from;
        private String to;
        private BigDecimal amount;
    }
}