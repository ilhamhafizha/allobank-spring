package com.allobanksplitbill.controller;

import com.allobanksplitbill.dto.request.CreateBillGroupRequest;
import com.allobanksplitbill.dto.respon.BillGroupResponse;
import com.allobanksplitbill.service.BillGroupService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bill-groups")
public class BillGroupController {

    private final BillGroupService billGroupService;

    public BillGroupController(BillGroupService billGroupService) {
        this.billGroupService = billGroupService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BillGroupResponse createGroup(
            @Valid @RequestBody CreateBillGroupRequest request) {
        return billGroupService.createGroup(request);
    }
}
