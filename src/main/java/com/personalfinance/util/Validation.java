package com.personalfinance.util;

import com.personalfinance.model.Category;
import com.personalfinance.model.TransactionType;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Objects;

public final class Validation {
    private Validation() { }
    public static String description(String value) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Description is required.");
        return value.strip();
    }
    public static void category(TransactionType type, Category category) {
        Objects.requireNonNull(type, "Type is required.");
        if (category == null || category.type() != type)
            throw new IllegalArgumentException("Select a category matching the transaction type.");
    }
    public static YearMonth month(YearMonth month) {
        Objects.requireNonNull(month, "Month is required.");
        if (month.getYear() < 1 || month.getYear() > 9999)
            throw new IllegalArgumentException("Year must be between 1 and 9999.");
        return month;
    }
    public static LocalDate date(LocalDate date) {
        Objects.requireNonNull(date, "Date is required.");
        month(YearMonth.from(date));
        return date;
    }
    public static long id(long id) {
        if (id <= 0) throw new IllegalArgumentException("ID must be positive.");
        return id;
    }
}
