package com.allobanksplitbill.service;

import com.allobanksplitbill.dto.request.CreateExpenseRequest;
import com.allobanksplitbill.dto.respon.ExpenseResponse;
import com.allobanksplitbill.dto.respon.ParticipantResponse;
import com.allobanksplitbill.entity.BillGroup;
import com.allobanksplitbill.entity.Expense;
import com.allobanksplitbill.entity.ExpenseParticipant;
import com.allobanksplitbill.entity.ExpenseParticipantId;
import com.allobanksplitbill.entity.Participant;
import com.allobanksplitbill.repository.BillGroupRepository;
import com.allobanksplitbill.repository.ExpenseRepository;
import com.allobanksplitbill.repository.ParticipantRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ExpenseService {

    private final BillGroupRepository billGroupRepository;
    private final ParticipantRepository participantRepository;
    private final ExpenseRepository expenseRepository;

    public ExpenseService(
            BillGroupRepository billGroupRepository,
            ParticipantRepository participantRepository,
            ExpenseRepository expenseRepository
    ) {
        this.billGroupRepository = billGroupRepository;
        this.participantRepository = participantRepository;
        this.expenseRepository = expenseRepository;
    }

    @Transactional
    public ExpenseResponse createExpense(
            Long groupId,
            CreateExpenseRequest request
    ) {
        BillGroup group = billGroupRepository.findById(groupId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Bill group not found"
                ));

        Participant payer = participantRepository.findById(request.getPaidBy())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Payer not found"
                ));

        if (!payer.getGroup().getId().equals(groupId)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Payer does not belong to this bill group"
            );
        }

        List<Long> participantIds = request.getParticipantIds();
        Set<Long> uniqueParticipantIds = new HashSet<>(participantIds);

        if (uniqueParticipantIds.size() != participantIds.size()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Duplicate participant IDs are not allowed"
            );
        }

        List<Participant> participants =
                participantRepository.findAllByGroupId(groupId);

        Set<Long> groupParticipantIds = new HashSet<>();
        for (Participant participant : participants) {
            groupParticipantIds.add(participant.getId());
        }

        if (!groupParticipantIds.containsAll(uniqueParticipantIds)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "All expense participants must belong to this bill group"
            );
        }

        Expense expense = new Expense();
        expense.setGroup(group);
        expense.setPaidBy(payer);
        expense.setDescription(request.getDescription());
        expense.setAmount(request.getAmount());

        List<ExpenseParticipant> expenseParticipants = new ArrayList<>();

        for (Participant participant : participants) {
            if (uniqueParticipantIds.contains(participant.getId())) {
                ExpenseParticipant expenseParticipant =
                        new ExpenseParticipant();

                ExpenseParticipantId id = new ExpenseParticipantId();
                id.setParticipantId(participant.getId());

                expenseParticipant.setId(id);
                expenseParticipant.setExpense(expense);
                expenseParticipant.setParticipant(participant);

                expenseParticipants.add(expenseParticipant);
            }
        }

        expense.setParticipants(expenseParticipants);

        Expense savedExpense = expenseRepository.save(expense);

        List<ParticipantResponse> participantResponses =
                savedExpense.getParticipants().stream()
                        .map(ep -> new ParticipantResponse(
                                ep.getParticipant().getId(),
                                ep.getParticipant().getName()
                        ))
                        .toList();

        ParticipantResponse payerResponse = new ParticipantResponse(
                savedExpense.getPaidBy().getId(),
                savedExpense.getPaidBy().getName()
        );

        return new ExpenseResponse(
                savedExpense.getId(),
                savedExpense.getDescription(),
                savedExpense.getAmount(),
                payerResponse,
                participantResponses
        );
    }
}
