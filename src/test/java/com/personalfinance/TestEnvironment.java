package com.personalfinance;

import com.personalfinance.adapter.*;
import com.personalfinance.decorator.*;
import com.personalfinance.model.*;
import com.personalfinance.repository.*;
import com.personalfinance.service.*;
import java.io.*;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.*;

final class TestEnvironment {
    static final YearMonth OCT = YearMonth.of(2026, 10);
    static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-23T10:00:00Z"), ZoneOffset.UTC);
    final ByteArrayOutputStream output = new ByteArrayOutputStream();
    final PrintStream console = new PrintStream(output, true, java.nio.charset.StandardCharsets.UTF_8);
    final TransactionRepository transactionRepository;
    final BudgetRepository budgetRepository;
    final RecurringTransactionRepository recurringRepository;
    final NotificationService notifications;
    final BudgetService budgets;
    final TransactionService transactions;
    final RecurringTransactionService recurring;
    final ReportService reports;
    final BankImportService bank;
    TestEnvironment(Path data) throws IOException {
        Path log = data.resolve("notifications.log");
        Notifier logged = new FileLoggingNotifierDecorator(new ConsoleNotifier(console), log, console);
        notifications = new NotificationService(new TimestampNotifierDecorator(logged, CLOCK),
                new PriorityNotifierDecorator(new TimestampNotifierDecorator(logged, CLOCK)), log);
        transactionRepository = new TransactionRepository(data);
        budgetRepository = new BudgetRepository(data);
        recurringRepository = new RecurringTransactionRepository(data);
        budgets = new BudgetService(budgetRepository, transactionRepository, notifications);
        transactions = new TransactionService(transactionRepository, budgets, notifications);
        recurring = new RecurringTransactionService(recurringRepository, transactions, notifications);
        reports = new ReportService(transactions);
        bank = new BankImportService(new BankAdapter(new FakeBankService()), transactions, notifications);
    }
    Transaction add(TransactionType type, String amount, Category category, String date) throws IOException {
        return transactions.add(type, new BigDecimal(amount), category, "Test transaction", LocalDate.parse(date), TransactionSource.MANUAL, "");
    }
    void sample() throws IOException {
        add(TransactionType.INCOME, "250000", Category.SALARY, "2026-10-01");
        add(TransactionType.EXPENSE, "35000", Category.FOOD, "2026-10-02");
        add(TransactionType.EXPENSE, "18000", Category.TRANSPORT, "2026-10-03");
        add(TransactionType.EXPENSE, "32000", Category.SHOPPING, "2026-10-04");
    }
}
