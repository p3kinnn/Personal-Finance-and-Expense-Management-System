package com.personalfinance.strategy;

import com.personalfinance.model.Category;
import com.personalfinance.model.Report;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Collectors;

public final class JsonExportStrategy implements ReportExportStrategy {
    @Override public void export(Report report, Path outputPath) throws IOException {
        String json = "{\n" +
                "  \"period\": \"" + report.month() + "\",\n" +
                "  \"year\": " + report.month().getYear() + ",\n" +
                "  \"month\": " + report.month().getMonthValue() + ",\n" +
                "  \"currency\": \"KZT\",\n" +
                "  \"totalIncome\": " + report.totalIncome().toPlainString() + ",\n" +
                "  \"totalExpenses\": " + report.totalExpenses().toPlainString() + ",\n" +
                "  \"netBalance\": " + report.netBalance().toPlainString() + ",\n" +
                "  \"transactionCount\": " + report.transactionCount() + ",\n" +
                "  \"incomeByCategory\": " + categories(report.incomeByCategory()) + ",\n" +
                "  \"expensesByCategory\": " + categories(report.expensesByCategory()) + "\n}\n";
        Files.writeString(outputPath, json, StandardCharsets.UTF_8);
    }
    private String categories(Map<Category, BigDecimal> categories) {
        // Category labels are controlled enum values without JSON control characters.
        return categories.entrySet().stream().map(e -> "\"" + e.getKey() + "\": " + e.getValue().toPlainString())
                .collect(Collectors.joining(", ", "{", "}"));
    }
}
