package com.personalfinance.model;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public record Report(YearMonth month, BigDecimal totalIncome, BigDecimal totalExpenses,
        Map<Category, BigDecimal> incomeByCategory, Map<Category, BigDecimal> expensesByCategory,
        int transactionCount) {
    public Report {
        EnumMap<Category, BigDecimal> incomes = new EnumMap<>(Category.class);
        incomes.putAll(incomeByCategory);
        incomeByCategory = Collections.unmodifiableMap(incomes);
        EnumMap<Category, BigDecimal> expenses = new EnumMap<>(Category.class);
        expenses.putAll(expensesByCategory);
        expensesByCategory = Collections.unmodifiableMap(expenses);
    }
    public BigDecimal netBalance() { return totalIncome.subtract(totalExpenses); }
}
