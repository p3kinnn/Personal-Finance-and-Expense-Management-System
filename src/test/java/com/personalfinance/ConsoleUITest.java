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

import com.personalfinance.ui.ConsoleUI;
import com.personalfinance.strategy.TxtExportStrategy;

class ConsoleUITest {
    @TempDir Path data;
    private String run(String input) throws IOException {
        TestEnvironment env = new TestEnvironment(data.resolve("data"));
        new ConsoleUI(new Scanner(input), env.console, env.transactions, env.budgets, env.recurring,
                env.reports, new ReportExportService(new TxtExportStrategy()), env.bank, env.notifications,
                data.resolve("exports"), TestEnvironment.CLOCK).run();
        return env.output.toString(java.nio.charset.StandardCharsets.UTF_8);
    }
    @Test void invalidInputsReturnToMenuWithoutCrashing() throws IOException {
        String output = run("x\n1\nabc\n2\n10\n99\n2\n10\n1\nFood\n2026-02-30\n8\n2026-13\n8\n0000-10\n4\n1\n999\n9\n2026-10\n99\n0\n");
        assertTrue(output.contains("Invalid menu choice"));
        assertTrue(output.contains("Invalid amount"));
        assertTrue(output.contains("Invalid category"));
        assertTrue(output.contains("Invalid date or month"));
        assertTrue(output.contains("Year must be"));
        assertTrue(output.contains("Transaction not found"));
        assertTrue(output.contains("Unsupported export format"));
        assertTrue(output.contains("Goodbye!"));
        assertTrue(new TestEnvironment(data.resolve("data")).transactions.all().isEmpty());
    }
    @Test void completeConsoleFlowAndRuntimeExportSwitching() throws IOException {
        String output = run("1\n250000\n1\nSalary\n2026-10-01\n5\n1\n2026-10\n50000\n2\n40000\n1\nGroceries\n2026-10-02\n11\n8\n2026-10\n9\n2026-10\n1\n9\n2026-10\n2\n9\n2026-10\n3\n10\n10\n12\n3\n0\n");
        assertTrue(output.contains("Current balance: 210000.00 KZT"));
        assertTrue(output.contains("budget has reached 80%"));
        assertTrue(output.contains("6 bank transactions imported. 0 duplicates skipped."));
        assertTrue(output.contains("0 bank transactions imported. 6 duplicates skipped."));
        for (String extension : List.of("txt", "csv", "json")) assertTrue(Files.exists(data.resolve("exports/report-2026-10."+extension)));
    }
    @Test void endOfInputExitsCleanly() throws IOException { assertTrue(run("").contains("Input closed. Data is saved.")); }
}
