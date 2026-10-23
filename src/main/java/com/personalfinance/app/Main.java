package com.personalfinance.app;

import com.personalfinance.adapter.*;
import com.personalfinance.decorator.*;
import com.personalfinance.repository.*;
import com.personalfinance.service.*;
import com.personalfinance.strategy.TxtExportStrategy;
import com.personalfinance.ui.ConsoleUI;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Scanner;

public final class Main {
    private Main() { }
    public static void main(String[] args) {
        try { start(args); }
        catch (IOException | IllegalArgumentException ex) {
            System.err.println("Unable to start: " + ex.getMessage());
            System.err.println("Check file permissions or restore corrupted files from a backup. Existing files are preserved.");
            System.exit(1);
        }
    }
    private static void start(String[] args) throws IOException {
        Path data = Path.of("data");
        Path exportDirectory = Path.of("exports");
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--help")) {
                System.out.println("Usage: java -jar target/personal-finance-manager-1.0.0.jar [--data-dir PATH] [--export-dir PATH]");
                return;
            }
            if (i + 1 >= args.length) throw new IllegalArgumentException("Missing path for " + args[i]);
            switch (args[i]) {
                case "--data-dir" -> data = Path.of(args[++i]);
                case "--export-dir" -> exportDirectory = Path.of(args[++i]);
                default -> throw new IllegalArgumentException("Unknown option: " + args[i]);
            }
        }
        Clock clock = Clock.systemDefaultZone();
        Path log = data.resolve("notifications.log");
        Notifier logged = new FileLoggingNotifierDecorator(new ConsoleNotifier(System.out), log, System.err);
        Notifier normal = new TimestampNotifierDecorator(logged, clock);
        Notifier important = new PriorityNotifierDecorator(new TimestampNotifierDecorator(logged, clock));
        NotificationService notifications = new NotificationService(normal, important, log);
        TransactionRepository transactionRepository = new TransactionRepository(data);
        BudgetRepository budgetRepository = new BudgetRepository(data);
        RecurringTransactionRepository recurringRepository = new RecurringTransactionRepository(data);
        BudgetService budgets = new BudgetService(budgetRepository, transactionRepository, notifications);
        TransactionService transactions = new TransactionService(transactionRepository, budgets, notifications);
        RecurringTransactionService recurring = new RecurringTransactionService(recurringRepository, transactions, notifications);
        ReportService reports = new ReportService(transactions);
        ReportExportService exports = new ReportExportService(new TxtExportStrategy());
        BankImportService bank = new BankImportService(new BankAdapter(new FakeBankService()), transactions, notifications);
        System.out.println(recurring.processDue(LocalDate.now(clock)) + " due recurring transactions processed at startup.");
        new ConsoleUI(new Scanner(System.in, StandardCharsets.UTF_8), System.out, transactions, budgets,
                recurring, reports, exports, bank, notifications, exportDirectory, clock).run();
    }
}
