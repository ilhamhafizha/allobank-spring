package com.allobanksplitbill.dto;

import com.allobanksplitbill.dto.request.CreateBillGroupRequest;
import com.allobanksplitbill.dto.request.CreateExpenseRequest;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RequestValidationTest {
    @Test
    void shouldRejectUnsupportedMonetaryPrecisionAndNullExpenseParticipant() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var validator = factory.getValidator();
            var request = new CreateExpenseRequest();
            request.setDescription("Dinner");
            request.setPaidBy(1L);
            request.setParticipantIds(List.of(1L));
            request.setAmount(new BigDecimal("10.999"));
            assertFalse(validator.validate(request).isEmpty());
            request.setAmount(new BigDecimal("100000000000000000.00"));
            assertFalse(validator.validate(request).isEmpty());
            request.setAmount(new BigDecimal("10.99"));
            assertTrue(validator.validate(request).isEmpty());
            request.setParticipantIds(Collections.singletonList(null));
            assertFalse(validator.validate(request).isEmpty());
        }
    }

    @Test
    void shouldRejectNullGroupParticipant() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var request = new CreateBillGroupRequest();
            request.setName("Trip");
            request.setParticipants(Collections.singletonList(null));
            assertFalse(factory.getValidator().validate(request).isEmpty());
        }
    }
}
