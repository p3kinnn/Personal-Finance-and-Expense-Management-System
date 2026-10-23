package com.personalfinance.model;

import com.personalfinance.util.Money;
import com.personalfinance.util.Validation;
import java.math.BigDecimal;
import java.time.YearMonth;

public record Budget(long id, Category category, YearMonth month, BigDecimal limit) {
    public Budget {
        Validation.id(id);
        Validation.category(TransactionType.EXPENSE, category);
        Validation.month(month);
        limit = Money.positive(limit);
    }
}
