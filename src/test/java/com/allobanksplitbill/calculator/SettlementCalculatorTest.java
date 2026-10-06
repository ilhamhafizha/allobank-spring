package com.allobanksplitbill.calculator;

import com.allobanksplitbill.dto.respon.SettlementResponse;
import com.allobanksplitbill.entity.Expense;
import com.allobanksplitbill.entity.ExpenseParticipant;
import com.allobanksplitbill.entity.Participant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SettlementCalculatorTest {

    private SettlementCalculator calculator;

    private Participant ilham;
    private Participant budi;
    private Participant andi;

    @BeforeEach
    void setUp() {
        calculator = new SettlementCalculator();

        ilham = createParticipant(1L, "Ilham");
        budi = createParticipant(2L, "Budi");
        andi = createParticipant(3L, "Andi");
    }

    @Test
    void shouldSplitExpenseAndServiceChargeEqually() {
        Expense expense = createExpense(
                "Dinner",
                "150000.00",
                ilham,
                List.of(ilham, budi, andi)
        );

        List<SettlementResponse.SettlementTransaction> result =
                calculator.calculate(
                        List.of(ilham, budi, andi),
                        List.of(expense),
                        new BigDecimal("4")
                );

        assertEquals(2, result.size());

        assertTransaction(result.get(0), "Budi", "Ilham", "52000.00");
        assertTransaction(result.get(1), "Andi", "Ilham", "52000.00");
    }

    @Test
    void shouldCalculateExpenseWhenPayerIsNotSharingIt() {
        Expense expense = createExpense(
                "Lunch",
                "150000.00",
                ilham,
                List.of(budi, andi)
        );

        List<SettlementResponse.SettlementTransaction> result =
                calculator.calculate(
                        List.of(ilham, budi, andi),
                        List.of(expense),
                        new BigDecimal("4")
                );

        assertEquals(2, result.size());

        assertTransaction(result.get(0), "Budi", "Ilham", "78000.00");
        assertTransaction(result.get(1), "Andi", "Ilham", "78000.00");
    }

    @Test
    void shouldHandleRoundingToTwoDecimalPlaces() {
        Expense expense = createExpense(
                "Shared expense",
                "100.00",
                ilham,
                List.of(ilham, budi, andi)
        );

        List<SettlementResponse.SettlementTransaction> result =
                calculator.calculate(
                        List.of(ilham, budi, andi),
                        List.of(expense),
                        BigDecimal.ZERO
                );

        assertEquals(2, result.size());

        assertTransaction(result.get(0), "Budi", "Ilham", "33.33");
        assertTransaction(result.get(1), "Andi", "Ilham", "33.34");
    }

    @Test
    void shouldReturnNoTransactionsWhenEveryoneSettlesTheirOwnExpense() {
        Expense expense = createExpense(
                "Solo expense",
                "50000.00",
                ilham,
                List.of(ilham)
        );

        List<SettlementResponse.SettlementTransaction> result =
                calculator.calculate(
                        List.of(ilham, budi, andi),
                        List.of(expense),
                        new BigDecimal("4")
                );

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldRejectExpenseWithoutParticipants() {
        Expense expense = createExpense(
                "Invalid expense",
                "100000.00",
                ilham,
                List.of()
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> calculator.calculate(
                        List.of(ilham, budi, andi),
                        List.of(expense),
                        new BigDecimal("4")
                )
        );
    }

    @Test
    void shouldAllocateGroupChargeWithoutRoundingEachExpenseIndependently() {
        Expense first = createExpense("First", "0.13", ilham, List.of(budi));
        first.setId(1L);
        Expense second = createExpense("Second", "0.13", ilham, List.of(budi));
        second.setId(2L);
        var result = calculator.calculate(List.of(ilham, budi), List.of(second, first),
                new BigDecimal("4"));
        assertEquals(1, result.size());
        assertTransaction(result.get(0), "Budi", "Ilham", "0.27");
        assertEquals(new BigDecimal("0.01"), calculator.calculateServiceChargeAmount(
                new BigDecimal("0.26"), new BigDecimal("4")));
    }

    @Test
    void shouldKeepRoundingDeterministicWhenParticipantOrderChanges() {
        var expense = createExpense("Dinner", "100.00", ilham, List.of(andi, ilham, budi));
        var result = calculator.calculate(List.of(andi, budi, ilham), List.of(expense), BigDecimal.ZERO);
        assertTransaction(result.get(0), "Budi", "Ilham", "33.33");
        assertTransaction(result.get(1), "Andi", "Ilham", "33.34");
    }

    @Test
    void shouldNetExpensesFromMultiplePayers() {
        var dinner = createExpense("Dinner", "90.00", ilham, List.of(ilham, budi, andi));
        var taxi = createExpense("Taxi", "30.00", budi, List.of(ilham, budi, andi));
        var result = calculator.calculate(List.of(ilham, budi, andi), List.of(dinner, taxi), BigDecimal.ZERO);
        assertEquals(2, result.size());
        assertTransaction(result.get(0), "Budi", "Ilham", "10.00");
        assertTransaction(result.get(1), "Andi", "Ilham", "40.00");
    }

    @Test
    void shouldIdentifyParticipantsWithTheSameNameById() {
        budi.setName("Ilham");
        var expense = createExpense("Dinner", "10.00", ilham, List.of(budi));
        var result = calculator.calculate(List.of(ilham, budi), List.of(expense), BigDecimal.ZERO);
        assertEquals(2L, result.get(0).getFromId());
        assertEquals(1L, result.get(0).getToId());
    }

    @Test
    void shouldReturnEmptySettlementForGroupWithoutExpenses() {
        assertTrue(calculator.calculate(List.of(ilham, budi), List.of(), new BigDecimal("4")).isEmpty());
    }

    private Participant createParticipant(Long id, String name) {
        Participant participant = new Participant();
        participant.setId(id);
        participant.setName(name);
        return participant;
    }

    private Expense createExpense(
            String description,
            String amount,
            Participant payer,
            List<Participant> sharedWith) {

        Expense expense = new Expense();
        expense.setDescription(description);
        expense.setAmount(new BigDecimal(amount));
        expense.setPaidBy(payer);

        List<ExpenseParticipant> expenseParticipants =
                sharedWith.stream()
                        .map(participant -> {
                            ExpenseParticipant ep = new ExpenseParticipant();
                            ep.setExpense(expense);
                            ep.setParticipant(participant);
                            return ep;
                        })
                        .toList();

        expense.setParticipants(expenseParticipants);
        return expense;
    }

    private void assertTransaction(
            SettlementResponse.SettlementTransaction actual,
            String expectedFrom,
            String expectedTo,
            String expectedAmount) {

        assertEquals(expectedFrom, actual.getFrom());
        assertEquals(expectedTo, actual.getTo());
        assertEquals(
                0,
                new BigDecimal(expectedAmount)
                        .compareTo(actual.getAmount())
        );
    }
}
