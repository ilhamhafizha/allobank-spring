package com.allobanksplitbill.controller;

import com.allobanksplitbill.dto.respon.SettlementResponse;
import com.allobanksplitbill.service.SettlementService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bill-groups/{groupId}/settlement")
public class SettlementController {

    private final SettlementService settlementService;

    public SettlementController(SettlementService settlementService) {
        this.settlementService = settlementService;
    }

    @GetMapping
    public SettlementResponse getSettlement(
            @PathVariable Long groupId) {
        return settlementService.getSettlement(groupId);
    }
}
