package com.allobanksplitbill.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CreateBillGroupRequest {

    @NotBlank
    @Size(max = 100)
    private String name;

    @NotEmpty
    private List<@NotNull @Valid ParticipantRequest> participants;

    @Getter
    @Setter
    public static class ParticipantRequest {

        @NotBlank
        @Size(max = 100)
        private String name;
    }
}