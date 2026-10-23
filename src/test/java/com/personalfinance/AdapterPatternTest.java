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

import com.personalfinance.adapter.*;

class AdapterPatternTest {
    @TempDir Path data;
    @Test void incompatibleBankBecomesUsableThroughTargetInterface() {
        FakeBankService bank = new FakeBankService();
        assertFalse(BankTransactionProvider.class.isInstance(bank));
        BankTransactionProvider provider = new BankAdapter(bank);
        List<BankImportRecord> items = provider.fetchTransactions();
        assertEquals(6, items.size());
        assertEquals(TransactionType.INCOME, items.get(0).type());
        assertEquals(new BigDecimal("250000.00"), items.get(0).amount());
        assertEquals(TransactionType.EXPENSE, items.get(1).type());
        assertEquals(new BigDecimal("12500.50"), items.get(1).amount());
        assertEquals(LocalDate.of(2026,10,3), items.get(1).date());
        assertEquals(Category.FOOD, items.get(1).category());
        assertEquals("KZ-202610-002", items.get(1).externalId());
    }
    @Test void repeatImportsSkipDuplicatesAfterRestartAndAffectBudgets() throws IOException {
        TestEnvironment env = new TestEnvironment(data);
        env.budgets.save(Category.FOOD, TestEnvironment.OCT, new BigDecimal("15000"));
        assertEquals(new BankImportService.ImportResult(6,0), env.bank.importTransactions());
        TestEnvironment reopened = new TestEnvironment(data);
        assertEquals(new BankImportService.ImportResult(0,6), reopened.bank.importTransactions());
        assertEquals(6, reopened.transactions.all().size());
        assertEquals(new BigDecimal("12500.50"), reopened.budgets.status(TestEnvironment.OCT).get(0).spent());
        assertTrue(reopened.notifications.history().contains("reached 80%"));
    }
    @Test void unknownMerchantUsesAppropriateFallbackAndBadDirectionIsRejected() {
        BankAdapter adapter = new BankAdapter(new FakeBankService());
        assertEquals(Category.OTHER_EXPENSE, adapter.convert(new ExternalBankRecord("x", 100, "01/10/2026", "Unknown", "DEBIT", "OTHER")).category());
        assertThrows(IllegalArgumentException.class, () -> adapter.convert(new ExternalBankRecord("x", 100, "01/10/2026", "Unknown", "SIDEWAYS", "OTHER")));
        assertThrows(DateTimeException.class, () -> adapter.convert(new ExternalBankRecord("x", 100, "31/02/2026", "Unknown", "DEBIT", "OTHER")));
    }
}
