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

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class TransactionServiceTest {
    @TempDir Path data;
    TestEnvironment env;
    @BeforeEach void setUp() throws IOException { env = new TestEnvironment(data); }
    @Test void addIncomeSavesTypeAndMoney() throws IOException {
        Transaction t = env.add(TransactionType.INCOME, "250000", Category.SALARY, "2026-10-01");
        assertEquals(TransactionType.INCOME, t.type());
        assertEquals(new BigDecimal("250000.00"), env.transactions.byId(t.id()).amount());
    }
    @Test void addExpenseSavesTypeAndSource() throws IOException {
        Transaction t = env.add(TransactionType.EXPENSE, "12.50", Category.FOOD, "2026-10-02");
        assertEquals(TransactionType.EXPENSE, t.type());
        assertEquals(TransactionSource.MANUAL, env.transactions.byId(t.id()).source());
    }
    @Test void calculateBalance() throws IOException {
        env.add(TransactionType.INCOME, "200000", Category.SALARY, "2026-10-01");
        env.add(TransactionType.EXPENSE, "50000", Category.FOOD, "2026-10-02");
        assertEquals(new BigDecimal("150000.00"), env.transactions.balance());
    }
    @ParameterizedTest @ValueSource(strings={"-1", "0", "abc", "1.234", "", "1e3", "NaN", "12,50"})
    void invalidAmountsAreRejectedWithoutSaving(String value) throws IOException {
        assertThrows(IllegalArgumentException.class, () -> Money.parse(value));
        assertTrue(env.transactions.all().isEmpty());
    }
    @Test void serviceRejectsInvalidMoneyCategoryAndDescription() throws IOException {
        assertThrows(IllegalArgumentException.class, () -> env.add(TransactionType.EXPENSE, "-1", Category.FOOD, "2026-10-01"));
        assertThrows(IllegalArgumentException.class, () -> env.add(TransactionType.INCOME, "100", Category.FOOD, "2026-10-01"));
        assertThrows(IllegalArgumentException.class, () -> env.transactions.add(TransactionType.INCOME,
                BigDecimal.ONE, Category.SALARY, " ", LocalDate.of(2026,10,1), TransactionSource.MANUAL, ""));
        assertTrue(env.transactions.all().isEmpty());
    }
    @Test void filtersAndHistorySortByDateThenId() throws IOException {
        Transaction late = env.add(TransactionType.EXPENSE, "100", Category.FOOD, "2026-11-05");
        Transaction first = env.add(TransactionType.INCOME, "500", Category.SALARY, "2026-10-01");
        Transaction second = env.add(TransactionType.EXPENSE, "20", Category.FOOD, "2026-10-01");
        assertEquals(List.of(first, second, late), env.transactions.all());
        assertEquals(List.of(second), env.transactions.filter(Category.FOOD, TestEnvironment.OCT, TransactionType.EXPENSE));
        assertEquals(List.of(first), env.transactions.filter(null, null, TransactionType.INCOME));
        assertEquals(List.of(second, late), env.transactions.filter(Category.FOOD, null, null));
    }
    @Test void missingTransactionHasClearError() {
        assertEquals("Transaction not found.", assertThrows(IllegalArgumentException.class, () -> env.transactions.byId(99)).getMessage());
    }
}
