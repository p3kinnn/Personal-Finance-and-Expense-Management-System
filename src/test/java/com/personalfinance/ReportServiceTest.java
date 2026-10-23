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

class ReportServiceTest {
    @TempDir Path data;
    @Test void reportGroupsCorrectMonthAndMatchesSample() throws IOException {
        TestEnvironment env = new TestEnvironment(data); env.sample();
        env.add(TransactionType.EXPENSE, "100", Category.FOOD, "2026-11-01");
        Report report = env.reports.generate(TestEnvironment.OCT);
        assertEquals(new BigDecimal("250000.00"), report.totalIncome());
        assertEquals(new BigDecimal("85000.00"), report.totalExpenses());
        assertEquals(new BigDecimal("165000.00"), report.netBalance());
        assertEquals(4, report.transactionCount());
        assertEquals(new BigDecimal("35000.00"), report.expensesByCategory().get(Category.FOOD));
        assertEquals(new BigDecimal("250000.00"), report.incomeByCategory().get(Category.SALARY));
        assertThrows(UnsupportedOperationException.class, () -> report.expensesByCategory().clear());
    }
    @Test void emptyReportIsZeroAndInvalidYearRejected() throws IOException {
        TestEnvironment env = new TestEnvironment(data);
        Report report = env.reports.generate(TestEnvironment.OCT);
        assertEquals(Money.ZERO, report.netBalance()); assertEquals(0, report.transactionCount());
        assertThrows(IllegalArgumentException.class, () -> env.reports.generate(YearMonth.of(0,10)));
    }
}
