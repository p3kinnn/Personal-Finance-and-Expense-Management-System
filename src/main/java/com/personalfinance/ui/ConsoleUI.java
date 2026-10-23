package com.personalfinance.ui;

import com.personalfinance.model.*;
import com.personalfinance.service.*;
import com.personalfinance.strategy.*;
import com.personalfinance.util.Money;
import com.personalfinance.util.ReportFormatter;
import com.personalfinance.util.Validation;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.time.Clock;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

public final class ConsoleUI {
    private final Scanner input;
    private final PrintStream output;
    private final TransactionService transactions;
    private final BudgetService budgets;
    private final RecurringTransactionService recurring;
    private final ReportService reports;
    private final ReportExportService exports;
    private final BankImportService bank;
    private final NotificationService notifications;
    private final Path exportDirectory;
    private final Clock clock;

    public ConsoleUI(Scanner input, PrintStream output, TransactionService transactions, BudgetService budgets,
            RecurringTransactionService recurring, ReportService reports, ReportExportService exports,
            BankImportService bank, NotificationService notifications, Path exportDirectory, Clock clock) {
        this.input = input; this.output = output; this.transactions = transactions; this.budgets = budgets;
        this.recurring = recurring; this.reports = reports; this.exports = exports; this.bank = bank;
        this.notifications = notifications; this.exportDirectory = exportDirectory; this.clock = clock;
    }
    public void run() {
        try {
            while (true) {
                output.println("\n=== PERSONAL FINANCE MANAGER (KZT) ===");
                output.println("1. Add Income\n2. Add Expense\n3. View Transaction History\n4. Search / Filter Transactions");
                output.println("5. Create or Update Monthly Budget\n6. View Budget Status\n7. Manage Recurring Transactions");
                output.println("8. Generate Financial Report\n9. Export Financial Report\n10. Import Bank Transactions");
                output.println("11. View Account Balance\n12. View Notifications\n0. Exit");
                try {
                    switch (read("Enter your choice: ")) {
                        case "1" -> addTransaction(TransactionType.INCOME);
                        case "2" -> addTransaction(TransactionType.EXPENSE);
                        case "3" -> showTransactions(transactions.all());
                        case "4" -> filter();
                        case "5" -> saveBudget();
                        case "6" -> showBudgets(month());
                        case "7" -> recurringMenu();
                        case "8" -> output.println(ReportFormatter.format(reports.generate(month())));
                        case "9" -> export();
                        case "10" -> bank.importTransactions();
                        case "11" -> output.println("Current balance: " + Money.format(transactions.balance()));
                        case "12" -> {
                            String history = notifications.history();
                            output.println(history.isBlank() ? "No notifications yet." : history);
                        }
                        case "0" -> { output.println("Goodbye! Data is saved."); return; }
                        default -> output.println("Invalid menu choice. Please try again.");
                    }
                } catch (DateTimeException ex) {
                    output.println("Invalid date or month. Use YYYY-MM-DD for dates, YYYY-MM for months (years 0001-9999).");
                } catch (IllegalArgumentException ex) {
                    output.println("Invalid input: " + ex.getMessage());
                } catch (IOException ex) {
                    output.println("Unable to complete operation: " + ex.getMessage());
                }
            }
        } catch (NoSuchElementException ex) { output.println("\nInput closed. Data is saved."); }
    }
    private String read(String prompt) { output.print(prompt); return input.nextLine().strip(); }
    private YearMonth month() { return Validation.month(YearMonth.parse(read("Month (YYYY-MM): "))); }
    private LocalDate date() {
        String text = read("Date (YYYY-MM-DD, blank = today): ");
        return Validation.date(text.isEmpty() ? LocalDate.now(clock) : LocalDate.parse(text));
    }
    private Category category(TransactionType type) {
        List<Category> categories = type == null ? Arrays.asList(Category.values()) : Category.forType(type);
        for (int i = 0; i < categories.size(); i++) output.println((i + 1) + ". " + categories.get(i));
        int selected = Integer.parseInt(read("Category number: "));
        if (selected < 1 || selected > categories.size()) throw new IllegalArgumentException("Invalid category selection.");
        return categories.get(selected - 1);
    }
    private void addTransaction(TransactionType type) throws IOException {
        var amount = Money.parse(read("Amount (KZT): "));
        Category category = category(type);
        String description = read("Description: ");
        LocalDate date = date();
        transactions.add(type, amount, category, description, date, TransactionSource.MANUAL, "");
        showBudgets(YearMonth.from(date));
    }
    private void showTransactions(List<Transaction> items) {
        if (items.isEmpty()) { output.println("No matching transactions."); return; }
        output.println("ID | DATE | TYPE | CATEGORY | AMOUNT | DESCRIPTION | SOURCE");
        for (Transaction t : items) output.printf("%d | %s | %s | %s | %s | %s | %s%n",
                t.id(), t.date(), t.type(), t.category(), Money.format(t.amount()), t.description(), t.source());
    }
    private void filter() throws IOException {
        output.println("1. By ID\n2. By month\n3. By category\n4. Income only\n5. Expenses only\n0. Back");
        switch (read("Filter: ")) {
            case "1" -> showTransactions(List.of(transactions.byId(Long.parseLong(read("Transaction ID: ")))));
            case "2" -> showTransactions(transactions.filter(null, month(), null));
            case "3" -> showTransactions(transactions.filter(category(null), null, null));
            case "4" -> showTransactions(transactions.filter(null, null, TransactionType.INCOME));
            case "5" -> showTransactions(transactions.filter(null, null, TransactionType.EXPENSE));
            case "0" -> { }
            default -> throw new IllegalArgumentException("Invalid filter choice.");
        }
    }
    private void saveBudget() throws IOException {
        Category category = category(TransactionType.EXPENSE);
        YearMonth month = month();
        budgets.save(category, month, Money.parse(read("Monthly spending limit (KZT): ")));
        showBudgets(month);
    }
    private void showBudgets(YearMonth month) throws IOException {
        List<BudgetStatus> items = budgets.status(month);
        if (items.isEmpty()) { output.println("No budgets for " + month + "."); return; }
        for (BudgetStatus status : items) output.printf("%s | %s | Limit: %s | Spent: %s | Remaining: %s | Usage: %s%% | %s%n",
                status.budget().category(), month, Money.format(status.budget().limit()), Money.format(status.spent()),
                Money.format(status.remaining()), status.usage().toPlainString(), status.level());
    }
    private void recurringMenu() throws IOException {
        while (true) {
            output.println("1. Add Recurring Income\n2. Add Recurring Expense\n3. View Recurring Transactions\n4. Disable Recurring Transaction\n5. Process Due Transactions\n0. Back");
            switch (read("Recurring choice: ")) {
                case "1" -> createRecurring(TransactionType.INCOME);
                case "2" -> createRecurring(TransactionType.EXPENSE);
                case "3" -> {
                    List<RecurringTransaction> items = recurring.all();
                    if (items.isEmpty()) output.println("No recurring transactions.");
                    for (RecurringTransaction r : items) output.printf("%d | %s | %s | %s | %s | Start: %s | Active: %s | Last: %s | %s%n",
                            r.id(), r.type(), r.category(), Money.format(r.amount()), "MONTHLY", r.startDate(),
                            r.active(), r.lastProcessedMonth(), r.description());
                }
                case "4" -> recurring.disable(Long.parseLong(read("Recurring ID: ")));
                case "5" -> output.println(recurring.processDue(LocalDate.now(clock)) + " due transactions processed.");
                case "0" -> { return; }
                default -> output.println("Invalid recurring menu choice.");
            }
        }
    }
    private void createRecurring(TransactionType type) throws IOException {
        var amount = Money.parse(read("Monthly amount (KZT): "));
        Category category = category(type);
        String description = read("Description: ");
        recurring.create(type, amount, category, description, date());
    }
    private void export() throws IOException {
        Report report = reports.generate(month());
        output.println("1. Export TXT\n2. Export CSV\n3. Export JSON\n0. Back");
        String extension;
        switch (read("Format: ")) {
            case "1" -> { exports.setStrategy(new TxtExportStrategy()); extension = "txt"; }
            case "2" -> { exports.setStrategy(new CsvExportStrategy()); extension = "csv"; }
            case "3" -> { exports.setStrategy(new JsonExportStrategy()); extension = "json"; }
            case "0" -> { return; }
            default -> throw new IllegalArgumentException("Unsupported export format.");
        }
        Path path = exportDirectory.resolve("report-" + report.month() + "." + extension);
        exports.exportReport(report, path);
        output.println("Report exported successfully: " + path);
    }
}
