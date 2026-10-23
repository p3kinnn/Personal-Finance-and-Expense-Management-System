package com.personalfinance;

import com.personalfinance.model.*;
import com.personalfinance.service.*;
import com.personalfinance.repository.*;
import com.personalfinance.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.math.BigDecimal;
import java.time.*;
import java.io.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class BudgetServiceTest {
    @TempDir Path data;
    TestEnvironment env;
    @BeforeEach void setUp() throws IOException { env = new TestEnvironment(data); }
    @Test void createAndUpdatePreserveIdentityAndPersist() throws IOException {
        Budget original = env.budgets.save(Category.FOOD, TestEnvironment.OCT, new BigDecimal("50000"));
        Budget updated = env.budgets.save(Category.FOOD, TestEnvironment.OCT, new BigDecimal("60000"));
        assertEquals(original.id(), updated.id());
        TestEnvironment reopened = new TestEnvironment(data);
        assertEquals(List.of(updated), reopened.budgetRepository.findAll());
    }
    @Test void budgetsAreIndependentPerMonthAndExpenseOnly() throws IOException {
        env.budgets.save(Category.FOOD, TestEnvironment.OCT, new BigDecimal("50000"));
        env.budgets.save(Category.FOOD, YearMonth.of(2026,11), new BigDecimal("10000"));
        env.add(TransactionType.INCOME, "250000", Category.SALARY, "2026-10-01");
        env.add(TransactionType.EXPENSE, "35000", Category.FOOD, "2026-10-02");
        env.add(TransactionType.EXPENSE, "1000", Category.FOOD, "2026-11-02");
        BudgetStatus status = env.budgets.status(TestEnvironment.OCT).get(0);
        assertEquals(new BigDecimal("35000.00"), status.spent());
        assertEquals(new BigDecimal("15000.00"), status.remaining());
        assertEquals(new BigDecimal("70.00"), status.usage());
        assertEquals(new BigDecimal("1000.00"), env.budgets.status(YearMonth.of(2026,11)).get(0).spent());
    }
    @Test void thresholdsCrossOnceAcrossRestart() throws IOException {
        env.budgets.save(Category.FOOD, TestEnvironment.OCT, new BigDecimal("100"));
        env.add(TransactionType.EXPENSE, "79", Category.FOOD, "2026-10-01");
        env.add(TransactionType.EXPENSE, "1", Category.FOOD, "2026-10-02");
        new TestEnvironment(data).add(TransactionType.EXPENSE, "1", Category.FOOD, "2026-10-03");
        env.add(TransactionType.EXPENSE, "19", Category.FOOD, "2026-10-04");
        env.add(TransactionType.EXPENSE, "1", Category.FOOD, "2026-10-05");
        env.add(TransactionType.EXPENSE, "1", Category.FOOD, "2026-10-06");
        String log = env.notifications.history();
        assertEquals(1, log.lines().filter(s -> s.contains("reached 80%")).count());
        assertEquals(1, log.lines().filter(s -> s.contains("limit reached (100%)")).count());
        assertEquals(1, log.lines().filter(s -> s.contains("has been exceeded!")).count());
        assertEquals(new BigDecimal("-2.00"), env.budgets.status(TestEnvironment.OCT).get(0).remaining());
    }
    @Test void jumpAcrossThresholdsEmitsHighestOnly() throws IOException {
        env.budgets.save(Category.FOOD, TestEnvironment.OCT, new BigDecimal("100"));
        env.add(TransactionType.EXPENSE, "105", Category.FOOD, "2026-10-01");
        String log = env.notifications.history();
        assertTrue(log.contains("has been exceeded!"));
        assertFalse(log.contains("reached 80%"));
        assertFalse(log.contains("limit reached (100%)"));
    }
    @Test void rejectInvalidBudgetLimitAndIncomeCategory() throws IOException {
        assertThrows(IllegalArgumentException.class, () -> env.budgets.save(Category.FOOD, TestEnvironment.OCT, BigDecimal.ZERO));
        assertThrows(IllegalArgumentException.class, () -> env.budgets.save(Category.SALARY, TestEnvironment.OCT, BigDecimal.TEN));
        assertTrue(env.budgetRepository.findAll().isEmpty());
    }
}
