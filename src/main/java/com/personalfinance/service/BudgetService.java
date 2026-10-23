package com.personalfinance.service;

import com.personalfinance.model.*;
import com.personalfinance.repository.BudgetRepository;
import com.personalfinance.repository.TransactionRepository;
import com.personalfinance.util.Money;
import com.personalfinance.util.Validation;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

public final class BudgetService {
    private final BudgetRepository budgets;
    private final TransactionRepository transactions;
    private final NotificationService notifications;
    public BudgetService(BudgetRepository budgets, TransactionRepository transactions, NotificationService notifications) {
        this.budgets = budgets; this.transactions = transactions; this.notifications = notifications;
    }
    public Budget save(Category category, YearMonth month, BigDecimal limit) throws IOException {
        List<Budget> items = budgets.findAll();
        Budget previous = items.stream().filter(b -> b.category() == category && b.month().equals(month)).findFirst().orElse(null);
        Budget budget = new Budget(previous == null ? budgets.nextId() : previous.id(), category, month, limit);
        items.removeIf(b -> b.id() == budget.id()); items.add(budget);
        budgets.saveAll(items);
        notifications.info("Budget saved successfully!");
        return budget;
    }
    public BigDecimal spent(Category category, YearMonth month) throws IOException {
        return transactions.findAll().stream()
                .filter(t -> t.type() == TransactionType.EXPENSE && t.category() == category && YearMonth.from(t.date()).equals(month))
                .map(Transaction::amount).reduce(Money.ZERO, BigDecimal::add);
    }
    public List<BudgetStatus> status(YearMonth month) throws IOException {
        Validation.month(month);
        java.util.ArrayList<BudgetStatus> result = new java.util.ArrayList<>();
        for (Budget b : budgets.findAll()) if (b.month().equals(month)) result.add(new BudgetStatus(b, spent(b.category(), month)));
        return result;
    }
    public void checkThreshold(Transaction transaction, BigDecimal before) throws IOException {
        if (transaction.type() != TransactionType.EXPENSE) return;
        YearMonth month = YearMonth.from(transaction.date());
        for (Budget b : budgets.findAll()) {
            if (b.category() != transaction.category() || !b.month().equals(month)) continue;
            BigDecimal after = before.add(transaction.amount());
            String message = null;
            // Emit only the highest newly crossed threshold for a single transaction.
            if (before.compareTo(b.limit()) <= 0 && after.compareTo(b.limit()) > 0) message = "budget has been exceeded!";
            else if (before.compareTo(b.limit()) < 0 && after.compareTo(b.limit()) == 0) message = "budget limit reached (100%).";
            else {
                BigDecimal warning = b.limit().multiply(new BigDecimal("0.80"));
                if (before.compareTo(warning) < 0 && after.compareTo(warning) >= 0) message = "budget has reached 80% [WARNING].";
            }
            if (message != null) notifications.important(b.category() + " " + month + " " + message);
        }
    }
}
