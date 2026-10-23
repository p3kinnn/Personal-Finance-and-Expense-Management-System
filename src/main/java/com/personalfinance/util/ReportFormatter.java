package com.personalfinance.util;

import com.personalfinance.model.Report;

public final class ReportFormatter {
    private ReportFormatter() { }
    public static String format(Report report) {
        StringBuilder text = new StringBuilder("FINANCIAL REPORT — " + report.month() + "\n");
        text.append("Total Income: ").append(Money.format(report.totalIncome())).append('\n');
        text.append("Total Expenses: ").append(Money.format(report.totalExpenses())).append('\n');
        text.append("Net Balance: ").append(Money.format(report.netBalance())).append('\n');
        text.append("Transactions: ").append(report.transactionCount()).append('\n');
        text.append("Income by Category:\n");
        report.incomeByCategory().forEach((category, amount) -> text.append(category).append(": ").append(Money.format(amount)).append('\n'));
        text.append("Expenses by Category:\n");
        report.expensesByCategory().forEach((category, amount) -> text.append(category).append(": ").append(Money.format(amount)).append('\n'));
        return text.toString();
    }
}
