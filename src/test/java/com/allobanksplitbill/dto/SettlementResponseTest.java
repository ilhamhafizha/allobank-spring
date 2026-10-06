package com.allobanksplitbill.dto;

import com.allobanksplitbill.dto.respon.SettlementResponse;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SettlementResponseTest {
    @Test
    void shouldSerializeRequiredServiceChargeFields() {
        var response = new SettlementResponse(1L, new BigDecimal("100.00"),
                new BigDecimal("4"), new BigDecimal("4.00"), List.of());
        var json = new ObjectMapper().valueToTree(response);
        assertEquals(4, json.get("service_charge_pct").intValue());
        assertEquals(0, new BigDecimal("4.00").compareTo(json.get("service_charge_amount").decimalValue()));
        assertFalse(json.has("serviceChargePct"));
        assertFalse(json.has("serviceChargeAmount"));
    }
}
