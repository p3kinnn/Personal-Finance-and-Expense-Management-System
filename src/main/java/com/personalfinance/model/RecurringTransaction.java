package com.personalfinance.model;

import com.personalfinance.util.Money;
import com.personalfinance.util.Validation;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

public record RecurringTransaction(long id, TransactionType type, BigDecimal amount, Category category,
        String description, LocalDate startDate, boolean active, YearMonth lastProcessedMonth) {
    public RecurringTransaction {
        Validation.id(id);
        Validation.category(type, category);
        amount = Money.positive(amount);
        description = Validation.description(description);
        Validation.date(startDate);
        if (lastProcessedMonth != null) {
            Validation.month(lastProcessedMonth);
            if (lastProcessedMonth.isBefore(YearMonth.from(startDate)))
                throw new IllegalArgumentException("Last processed month precedes start date.");
        }
    }
    public RecurringTransaction processed(YearMonth month) {
        return new RecurringTransaction(id, type, amount, category, description, startDate, active, month);
    }
    public RecurringTransaction disabled() {
        return new RecurringTransaction(id, type, amount, category, description, startDate, false, lastProcessedMonth);
    }
}
