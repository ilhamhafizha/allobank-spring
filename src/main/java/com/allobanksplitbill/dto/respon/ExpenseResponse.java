package com.allobanksplitbill.dto.respon;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@AllArgsConstructor
public class ExpenseResponse {

    private Long id;
    private String description;
    private BigDecimal amount;
    private ParticipantResponse paidBy;
    private List<ParticipantResponse> participants;
}