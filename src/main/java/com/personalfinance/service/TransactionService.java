package com.personalfinance.service;

import com.personalfinance.model.*;
import com.personalfinance.repository.TransactionRepository;
import com.personalfinance.util.Money;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;

public final class TransactionService {
    private final TransactionRepository repository;
    private final BudgetService budgets;
    private final NotificationService notifications;
    public TransactionService(TransactionRepository repository, BudgetService budgets, NotificationService notifications) {
        this.repository = repository; this.budgets = budgets; this.notifications = notifications;
    }
    public Transaction add(TransactionType type, BigDecimal amount, Category category, String description,
            LocalDate date, TransactionSource source, String externalId) throws IOException {
        Transaction transaction = new Transaction(repository.nextId(), type, amount, category, description, date, source, externalId);
        List<Transaction> items = repository.findAll();
        if (!transaction.externalId().isEmpty() && hasExternalId(source, transaction.externalId()))
            throw new IllegalArgumentException("Transaction already imported or processed.");
        // Read budget data before committing, so corrupted budgets cannot cause a partial operation.
        BigDecimal before = budgets.spent(category, YearMonth.from(date));
        budgets.status(YearMonth.from(date));
        items.add(transaction); repository.saveAll(items);
        notifications.info((type == TransactionType.INCOME ? "Income" : "Expense") + " added successfully! ID: " + transaction.id());
        budgets.checkThreshold(transaction, before);
        return transaction;
    }
    public boolean hasExternalId(TransactionSource source, String externalId) throws IOException {
        return repository.findAll().stream().anyMatch(t -> t.source() == source && t.externalId().equals(externalId));
    }
    public List<Transaction> all() throws IOException {
        return repository.findAll().stream().sorted(Comparator.comparing(Transaction::date).thenComparingLong(Transaction::id)).toList();
    }
    public Transaction byId(long id) throws IOException {
        return all().stream().filter(t -> t.id() == id).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found."));
    }
    public List<Transaction> filter(Category category, YearMonth month, TransactionType type) throws IOException {
        return all().stream().filter(t -> category == null || t.category() == category)
                .filter(t -> month == null || YearMonth.from(t.date()).equals(month))
                .filter(t -> type == null || t.type() == type).toList();
    }
    public BigDecimal balance() throws IOException {
        BigDecimal balance = Money.ZERO;
        for (Transaction t : all()) balance = t.type() == TransactionType.INCOME ? balance.add(t.amount()) : balance.subtract(t.amount());
        return balance;
    }
}
