package com.allobanksplitbill.entity;


import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "expense_participants")
@Getter
@Setter
@NoArgsConstructor
public class ExpenseParticipant {

    @EmbeddedId
    private ExpenseParticipantId id;

    @ManyToOne(fetch = jakarta.persistence.FetchType.LAZY, optional = false)
    @MapsId("expenseId")
    @JoinColumn(name = "expense_id", nullable = false)
    private Expense expense;

    @ManyToOne(fetch = jakarta.persistence.FetchType.LAZY, optional = false)
    @MapsId("participantId")
    @JoinColumn(name = "participant_id", nullable = false)
    private Participant participant;
}