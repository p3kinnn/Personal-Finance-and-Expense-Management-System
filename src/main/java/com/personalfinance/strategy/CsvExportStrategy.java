package com.personalfinance.strategy;

import com.personalfinance.model.Report;
import com.personalfinance.util.Csv;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class CsvExportStrategy implements ReportExportStrategy {
    @Override public void export(Report report, Path outputPath) throws IOException {
        List<List<String>> rows = new ArrayList<>();
        rows.add(List.of("period", "section", "category", "value", "currency"));
        String period = report.month().toString();
        rows.add(List.of(period, "totalIncome", "", report.totalIncome().toPlainString(), "KZT"));
        rows.add(List.of(period, "totalExpenses", "", report.totalExpenses().toPlainString(), "KZT"));
        rows.add(List.of(period, "netBalance", "", report.netBalance().toPlainString(), "KZT"));
        rows.add(List.of(period, "transactionCount", "", Integer.toString(report.transactionCount()), ""));
        report.incomeByCategory().forEach((c, a) -> rows.add(List.of(period, "income", c.toString(), a.toPlainString(), "KZT")));
        report.expensesByCategory().forEach((c, a) -> rows.add(List.of(period, "expense", c.toString(), a.toPlainString(), "KZT")));
        Csv.write(outputPath, rows);
    }
}
