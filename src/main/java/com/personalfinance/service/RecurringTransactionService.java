package com.personalfinance.service;

import com.personalfinance.model.*;
import com.personalfinance.repository.RecurringTransactionRepository;
import com.personalfinance.util.Validation;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

public final class RecurringTransactionService {
    private final RecurringTransactionRepository repository;
    private final TransactionService transactions;
    private final NotificationService notifications;
    public RecurringTransactionService(RecurringTransactionRepository repository, TransactionService transactions, NotificationService notifications) {
        this.repository = repository; this.transactions = transactions; this.notifications = notifications;
    }
    public RecurringTransaction create(TransactionType type, BigDecimal amount, Category category, String description, LocalDate start) throws IOException {
        RecurringTransaction item = new RecurringTransaction(repository.nextId(), type, amount, category, description, start, true, null);
        List<RecurringTransaction> items = all(); items.add(item); repository.saveAll(items);
        notifications.info("Recurring transaction created. Use Process Due Transactions or restart to apply it.");
        return item;
    }
    public List<RecurringTransaction> all() throws IOException { return repository.findAll(); }
    public void disable(long id) throws IOException {
        List<RecurringTransaction> items = all();
        for (int i = 0; i < items.size(); i++) if (items.get(i).id() == id) {
            items.set(i, items.get(i).disabled()); repository.saveAll(items);
            notifications.info("Recurring transaction disabled."); return;
        }
        throw new IllegalArgumentException("Recurring transaction not found.");
    }
    public int processDue(LocalDate today) throws IOException {
        Validation.date(today);
        int count = 0;
        List<RecurringTransaction> items = all();
        for (int i = 0; i < items.size(); i++) {
            RecurringTransaction item = items.get(i);
            if (!item.active()) continue;
            YearMonth month = item.lastProcessedMonth() == null ? YearMonth.from(item.startDate()) : item.lastProcessedMonth().plusMonths(1);
            while (!month.isAfter(YearMonth.from(today))) {
                LocalDate due = month.atDay(Math.min(item.startDate().getDayOfMonth(), month.lengthOfMonth()));
                if (due.isAfter(today)) break;
                String occurrenceId = "recurring:" + item.id() + ":" + month;
                // The occurrence key survives a crash between transaction save and marker save.
                if (!transactions.hasExternalId(TransactionSource.RECURRING, occurrenceId)) {
                    transactions.add(item.type(), item.amount(), item.category(), item.description(), due,
                            TransactionSource.RECURRING, occurrenceId);
                    count++;
                    notifications.info("Recurring transaction " + item.id() + " processed for " + month + ".");
                }
                item = item.processed(month); items.set(i, item); repository.saveAll(items);
                month = month.plusMonths(1);
            }
        }
        return count;
    }
}
