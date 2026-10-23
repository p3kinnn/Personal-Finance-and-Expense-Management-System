package com.personalfinance.service;

import com.personalfinance.model.*;
import com.personalfinance.util.Money;
import com.personalfinance.util.Validation;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class ReportService {
    private final TransactionService transactions;
    public ReportService(TransactionService transactions) { this.transactions = transactions; }
    public Report generate(YearMonth month) throws IOException {
        Validation.month(month);
        List<Transaction> items = transactions.filter(null, month, null);
        Map<Category, BigDecimal> income = new EnumMap<>(Category.class);
        Map<Category, BigDecimal> expenses = new EnumMap<>(Category.class);
        BigDecimal totalIncome = Money.ZERO;
        BigDecimal totalExpenses = Money.ZERO;
        for (Transaction t : items) {
            if (t.type() == TransactionType.INCOME) {
                totalIncome = totalIncome.add(t.amount()); income.merge(t.category(), t.amount(), BigDecimal::add);
            } else {
                totalExpenses = totalExpenses.add(t.amount()); expenses.merge(t.category(), t.amount(), BigDecimal::add);
            }
        }
        return new Report(month, totalIncome, totalExpenses, income, expenses, items.size());
    }
}
