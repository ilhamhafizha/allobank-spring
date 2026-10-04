package com.allobanksplitbill.service;

import com.allobanksplitbill.dto.request.CreateBillGroupRequest;
import com.allobanksplitbill.dto.respon.BillGroupResponse;
import com.allobanksplitbill.dto.respon.ParticipantResponse;
import com.allobanksplitbill.entity.BillGroup;
import com.allobanksplitbill.entity.Participant;
import com.allobanksplitbill.repository.BillGroupRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BillGroupService {

    private final BillGroupRepository billGroupRepository;

    public BillGroupService(BillGroupRepository billGroupRepository) {
        this.billGroupRepository = billGroupRepository;
    }

    @Transactional
    public BillGroupResponse createGroup(CreateBillGroupRequest request) {
        BillGroup group = new BillGroup();
        group.setName(request.getName());

        for (CreateBillGroupRequest.ParticipantRequest participantRequest
                : request.getParticipants()) {

            Participant participant = new Participant();
            participant.setName(participantRequest.getName());
            participant.setGroup(group);

            group.getParticipants().add(participant);
        }

        BillGroup savedGroup = billGroupRepository.save(group);

        List<ParticipantResponse> participants =
                savedGroup.getParticipants().stream()
                        .map(participant -> new ParticipantResponse(
                                participant.getId(),
                                participant.getName()
                        ))
                        .toList();

        return new BillGroupResponse(
                savedGroup.getId(),
                savedGroup.getName(),
                participants
        );
    }
}
