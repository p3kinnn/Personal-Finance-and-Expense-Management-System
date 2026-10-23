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

class RecurringTransactionServiceTest {
    @TempDir Path data;
    TestEnvironment env;
    @BeforeEach void setUp() throws IOException { env = new TestEnvironment(data); }
    @Test void monthlyCatchUpClampsDayAndNeverCreatesFutureTransactions() throws IOException {
        env.recurring.create(TransactionType.EXPENSE, new BigDecimal("80000"), Category.RENT, "Rent", LocalDate.of(2026,1,31));
        assertEquals(2, env.recurring.processDue(LocalDate.of(2026,3,30)));
        assertEquals(List.of(LocalDate.of(2026,1,31), LocalDate.of(2026,2,28)), env.transactions.all().stream().map(Transaction::date).toList());
        assertEquals(1, env.recurring.processDue(LocalDate.of(2026,3,31)));
        assertEquals(0, new TestEnvironment(data).recurring.processDue(LocalDate.of(2026,3,31)));
        assertEquals(3, env.transactions.all().size());
    }
    @Test void futureStartAndDisabledRecurrenceAreNotApplied() throws IOException {
        var future = env.recurring.create(TransactionType.INCOME, BigDecimal.TEN, Category.GIFT, "Future", LocalDate.of(2026,11,1));
        var disabled = env.recurring.create(TransactionType.INCOME, BigDecimal.TEN, Category.GIFT, "Disabled", LocalDate.of(2026,10,1));
        env.recurring.disable(disabled.id());
        assertEquals(0, env.recurring.processDue(LocalDate.of(2026,10,23)));
        assertTrue(env.transactions.all().isEmpty());
        assertTrue(env.recurring.all().stream().filter(r -> r.id()==future.id()).findFirst().orElseThrow().active());
    }
    @Test void occurrenceKeyRecoversFromStaleMarker() throws IOException {
        var original = env.recurring.create(TransactionType.INCOME, new BigDecimal("50000"), Category.SCHOLARSHIP, "Scholarship", LocalDate.of(2026,10,1));
        assertEquals(1, env.recurring.processDue(LocalDate.of(2026,10,23)));
        env.recurringRepository.saveAll(List.of(original)); // Simulate a failed marker save after transaction commit.
        TestEnvironment reopened = new TestEnvironment(data);
        assertEquals(0, reopened.recurring.processDue(LocalDate.of(2026,10,23)));
        assertEquals(1, reopened.transactions.all().size());
        assertEquals(TestEnvironment.OCT, reopened.recurring.all().get(0).lastProcessedMonth());
    }
    @Test void leapYearAndInvalidRecurringId() throws IOException {
        env.recurring.create(TransactionType.INCOME, BigDecimal.TEN, Category.GIFT, "Leap", LocalDate.of(2024,1,31));
        assertEquals(2, env.recurring.processDue(LocalDate.of(2024,2,29)));
        assertEquals(LocalDate.of(2024,2,29), env.transactions.all().get(1).date());
        assertThrows(IllegalArgumentException.class, () -> env.recurring.disable(999));
    }
}
