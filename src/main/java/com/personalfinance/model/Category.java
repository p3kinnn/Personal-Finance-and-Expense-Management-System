package com.personalfinance.model;

import java.util.Arrays;
import java.util.List;

public enum Category {
    SALARY("Salary", TransactionType.INCOME),
    SCHOLARSHIP("Scholarship", TransactionType.INCOME),
    GIFT("Gift", TransactionType.INCOME),
    OTHER_INCOME("Other Income", TransactionType.INCOME),
    FOOD("Food", TransactionType.EXPENSE),
    TRANSPORT("Transport", TransactionType.EXPENSE),
    SHOPPING("Shopping", TransactionType.EXPENSE),
    ENTERTAINMENT("Entertainment", TransactionType.EXPENSE),
    EDUCATION("Education", TransactionType.EXPENSE),
    RENT("Rent", TransactionType.EXPENSE),
    UTILITIES("Utilities", TransactionType.EXPENSE),
    HEALTH("Health", TransactionType.EXPENSE),
    OTHER_EXPENSE("Other Expense", TransactionType.EXPENSE);

    private final String label;
    private final TransactionType type;

    Category(String label, TransactionType type) { this.label = label; this.type = type; }
    public TransactionType type() { return type; }
    public static List<Category> forType(TransactionType type) {
        return Arrays.stream(values()).filter(c -> c.type == type).toList();
    }
    @Override public String toString() { return label; }
}
