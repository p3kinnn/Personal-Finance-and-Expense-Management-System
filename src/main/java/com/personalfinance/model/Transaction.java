package com.personalfinance.model;

import com.personalfinance.util.Money;
import com.personalfinance.util.Validation;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public record Transaction(long id, TransactionType type, BigDecimal amount, Category category,
                          String description, LocalDate date, TransactionSource source, String externalId) {
    public Transaction {
        Validation.id(id);
        Validation.category(type, category);
        amount = Money.positive(amount);
        description = Validation.description(description);
        Validation.date(date);
        Objects.requireNonNull(source, "Source is required.");
        externalId = externalId == null ? "" : externalId.strip();
        if (source != TransactionSource.MANUAL && externalId.isEmpty())
            throw new IllegalArgumentException("Bank and recurring transactions require an external ID.");
    }
}
