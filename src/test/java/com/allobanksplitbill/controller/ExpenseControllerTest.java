package com.allobanksplitbill.controller;

import com.allobanksplitbill.exception.ApiExceptionHandler;
import com.allobanksplitbill.service.ExpenseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ExpenseControllerTest {
    private final ExpenseService service = mock(ExpenseService.class);
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(new ExpenseController(service))
                .setControllerAdvice(new ApiExceptionHandler()).build();
    }

    @Test
    void shouldExplainMissingRequiredFields() throws Exception {
        mvc.perform(post("/api/bill-groups/1/expenses")
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Request validation failed. Check the errors field."))
                .andExpect(jsonPath("$.errors").isNotEmpty());
        verifyNoInteractions(service);
    }

    @Test
    void shouldExplainMalformedJson() throws Exception {
        mvc.perform(post("/api/bill-groups/1/expenses")
                        .contentType(MediaType.APPLICATION_JSON).content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").isNotEmpty());
        verifyNoInteractions(service);
    }

    @Test
    void shouldExplainParticipantsOutsideGroup() throws Exception {
        when(service.createExpense(eq(1L), any())).thenThrow(new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "All expense participants must belong to this bill group"));
        mvc.perform(post("/api/bill-groups/1/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"Dinner\",\"amount\":150000,\"paidBy\":1,\"participantIds\":[1,2,3]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("All expense participants must belong to this bill group"));
    }
}
