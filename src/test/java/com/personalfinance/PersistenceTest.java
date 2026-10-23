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

class PersistenceTest {
    @TempDir Path data;
    @Test void allEntitiesReloadAndIdsContinue() throws IOException {
        TestEnvironment env = new TestEnvironment(data); env.sample();
        env.budgets.save(Category.FOOD, TestEnvironment.OCT, new BigDecimal("50000"));
        env.recurring.create(TransactionType.INCOME, BigDecimal.TEN, Category.GIFT, "Gift", LocalDate.of(2026,10,1));
        env.recurring.processDue(LocalDate.of(2026,10,23));
        TestEnvironment reopened = new TestEnvironment(data);
        assertEquals(env.transactions.all(), reopened.transactions.all());
        assertEquals(env.budgetRepository.findAll(), reopened.budgetRepository.findAll());
        assertEquals(env.recurring.all(), reopened.recurring.all());
        assertEquals(env.transactions.balance(), reopened.transactions.balance());
        assertEquals(6, reopened.add(TransactionType.EXPENSE, "1", Category.FOOD, "2026-10-23").id());
        assertEquals(0, reopened.recurring.processDue(LocalDate.of(2026,10,23)));
    }
    @Test void csvRoundTripsCommasQuotesUnicodeAndNewlines() throws IOException {
        TestEnvironment env = new TestEnvironment(data);
        String description = "Groceries, \"market\"\nАлматы ₸\r\nSecond line";
        Transaction t = env.transactions.add(TransactionType.EXPENSE, BigDecimal.TEN, Category.FOOD,
                description, LocalDate.of(2026,10,1), TransactionSource.MANUAL, "");
        assertEquals(t, new TestEnvironment(data).transactions.byId(t.id()));
        assertEquals(List.of(List.of("a", "", "b,c", "x\"y")), Csv.parse("a,,\"b,c\",\"x\"\"y\"\r\n"));
    }
    @Test void missingFilesAreCreatedWithHeaders() throws IOException {
        new TestEnvironment(data);
        assertTrue(Files.readString(data.resolve("transactions.csv")).startsWith("id,type,amount"));
        assertTrue(Files.exists(data.resolve("budgets.csv")));
        assertTrue(Files.exists(data.resolve("recurring_transactions.csv")));
        assertTrue(Files.exists(data.resolve("notifications.log")));
    }
    @Test void corruptRecordIsReportedAndNeverOverwritten() throws IOException {
        new TransactionRepository(data);
        Path path = data.resolve("transactions.csv");
        String broken = Files.readString(path) + "1,EXPENSE,not-money,FOOD,Test,2026-10-01,MANUAL,\n";
        Files.writeString(path, broken);
        IOException error = assertThrows(IOException.class, () -> new TransactionRepository(data));
        assertTrue(error.getMessage().contains("record 2"));
        assertEquals(broken, Files.readString(path));
    }
    @Test void duplicateIdsAndExternalIdsAreRejected() throws IOException {
        TransactionRepository repo = new TransactionRepository(data);
        Transaction t = new Transaction(1, TransactionType.INCOME, BigDecimal.TEN, Category.SALARY, "Salary", LocalDate.of(2026,10,1), TransactionSource.BANK, "bank1");
        assertThrows(IllegalArgumentException.class, () -> repo.saveAll(List.of(t,t)));
        Transaction sameExternal = new Transaction(2, t.type(), t.amount(), t.category(), t.description(), t.date(), t.source(), t.externalId());
        assertThrows(IllegalArgumentException.class, () -> repo.saveAll(List.of(t,sameExternal)));
        assertTrue(repo.findAll().isEmpty());
    }
    @Test void malformedCsvAndHeaderRejected() throws IOException {
        assertThrows(IOException.class, () -> Csv.parse("\"unclosed"));
        assertThrows(IOException.class, () -> Csv.parse("a\"b"));
        assertThrows(IOException.class, () -> Csv.parse("\"a\"b"));
        Files.writeString(data.resolve("budgets.csv"), "wrong,header\n");
        assertThrows(IOException.class, () -> new BudgetRepository(data));
    }
    @Test void duplicateBudgetsAndInvalidActiveFlagRejected() throws IOException {
        BudgetRepository budgets = new BudgetRepository(data);
        assertThrows(IllegalArgumentException.class, () -> budgets.saveAll(List.of(
            new Budget(1, Category.FOOD, TestEnvironment.OCT, BigDecimal.TEN),
            new Budget(2, Category.FOOD, TestEnvironment.OCT, BigDecimal.ONE))));
        new RecurringTransactionRepository(data);
        Path path = data.resolve("recurring_transactions.csv");
        Files.writeString(path, Files.readString(path)+"1,INCOME,10.00,GIFT,Gift,2026-10-01,maybe,\n");
        assertThrows(IOException.class, () -> new RecurringTransactionRepository(data));
    }
}
