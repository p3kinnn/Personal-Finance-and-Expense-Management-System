package com.personalfinance;

import com.personalfinance.model.*;
import com.personalfinance.service.*;
import com.personalfinance.repository.*;
import com.personalfinance.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.math.BigDecimal;
import java.time.*;
import java.io.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

import com.personalfinance.strategy.*;

class StrategyPatternTest {
    @TempDir Path data;
    @Test void sameContextSwitchesBetweenThreeRealFormats() throws IOException {
        TestEnvironment env = new TestEnvironment(data.resolve("data")); env.sample();
        Report report = env.reports.generate(TestEnvironment.OCT);
        ReportExportService context = new ReportExportService(new TxtExportStrategy());
        Path txt = data.resolve("exports/report.txt"); context.exportReport(report, txt);
        assertTrue(Files.readString(txt).contains("Net Balance: 165000.00 KZT"));
        context.setStrategy(new CsvExportStrategy());
        Path csv = data.resolve("exports/report.csv"); context.exportReport(report, csv);
        List<List<String>> rows = Csv.parse(Files.readString(csv));
        assertEquals(List.of("period", "section", "category", "value", "currency"), rows.get(0));
        assertTrue(rows.contains(List.of("2026-10", "expense", "Food", "35000.00", "KZT")));
        assertTrue(rows.contains(List.of("2026-10", "transactionCount", "", "4", "")));
        context.setStrategy(new JsonExportStrategy());
        Path json = data.resolve("exports/report.json"); context.exportReport(report, json);
        String content = Files.readString(json);
        assertTrue(content.startsWith("{\n")); assertTrue(content.endsWith("}\n"));
        assertTrue(content.contains("\"period\": \"2026-10\""));
        assertTrue(content.contains("\"currency\": \"KZT\""));
        assertTrue(content.contains("\"totalIncome\": 250000.00"));
        assertTrue(content.contains("\"totalExpenses\": 85000.00"));
        assertTrue(content.contains("\"netBalance\": 165000.00"));
        assertTrue(content.contains("\"transactionCount\": 4"));
        assertTrue(content.contains("\"Food\": 35000.00"));
        assertTrue(content.contains("\"incomeByCategory\": {\"Salary\": 250000.00}"));
    }
    @Test void newStrategyWorksWithoutChangingContext() throws IOException {
        ReportExportService context = new ReportExportService((report, path) -> Files.writeString(path, "Count=" + report.transactionCount()));
        Path path = data.resolve("fourth.custom");
        context.exportReport(new TestEnvironment(data.resolve("data")).reports.generate(TestEnvironment.OCT), path);
        assertEquals("Count=0", Files.readString(path));
    }
    @Test void unwritableExportLocationIsReported() throws IOException {
        Path blocker = data.resolve("file"); Files.writeString(blocker, "Existing content");
        Report report = new TestEnvironment(data.resolve("data")).reports.generate(TestEnvironment.OCT);
        assertThrows(IOException.class, () -> new ReportExportService(new TxtExportStrategy()).exportReport(report, blocker.resolve("report.txt")));
        assertEquals("Existing content", Files.readString(blocker));
    }
}
