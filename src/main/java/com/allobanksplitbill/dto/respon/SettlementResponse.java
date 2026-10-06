package com.allobanksplitbill.dto.respon;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@AllArgsConstructor
public class SettlementResponse {

    private Long groupId;
    private BigDecimal totalExpense;
    @JsonProperty("service_charge_pct")
    private BigDecimal serviceChargePct;
    @JsonProperty("service_charge_amount")
    private BigDecimal serviceChargeAmount;
    private List<SettlementTransaction> transactions;

    @Getter
    @AllArgsConstructor
    public static class SettlementTransaction {

        private Long fromId;
        private String from;
        private Long toId;
        private String to;
        private BigDecimal amount;
    }
}