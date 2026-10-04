package com.allobanksplitbill.dto.respon;


import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class BillGroupResponse {

    private Long id;
    private String name;
    private List<ParticipantResponse> participants;
}