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

import com.personalfinance.decorator.*;

class DecoratorPatternTest {
    @TempDir Path data;
    @Test void baseNotifierPrintsOriginalMessage() {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        new ConsoleNotifier(new PrintStream(bytes)).notify("Saved");
        assertEquals("Saved" + System.lineSeparator(), bytes.toString());
    }
    @Test void timestampDecoratorAddsDeterministicTime() {
        List<String> messages = new ArrayList<>();
        new TimestampNotifierDecorator(messages::add, TestEnvironment.CLOCK).notify("Saved");
        assertEquals(List.of("[2026-10-23T10:00:00] Saved"), messages);
    }
    @Test void priorityDecoratorAddsMarker() {
        List<String> messages = new ArrayList<>();
        new PriorityNotifierDecorator(messages::add).notify("Limit");
        assertEquals(List.of("[IMPORTANT] Limit"), messages);
    }
    @Test void loggingDecoratorAppendsAndDelegates() throws IOException {
        List<String> messages = new ArrayList<>(); Path log = data.resolve("notifications.log");
        Notifier notifier = new FileLoggingNotifierDecorator(messages::add, log, System.err);
        notifier.notify("First"); notifier.notify("Second");
        assertEquals(List.of("First", "Second"), Files.readAllLines(log));
        assertEquals(List.of("First", "Second"), messages);
    }
    @Test void combinedDecoratorsProduceIdenticalConsoleAndLogContent() throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream(); Path log = data.resolve("notifications.log");
        Notifier notifier = new PriorityNotifierDecorator(new TimestampNotifierDecorator(
                new FileLoggingNotifierDecorator(new ConsoleNotifier(new PrintStream(bytes)), log, System.err), TestEnvironment.CLOCK));
        notifier.notify("Budget exceeded");
        assertEquals("[2026-10-23T10:00:00] [IMPORTANT] Budget exceeded" + System.lineSeparator(), bytes.toString());
        assertEquals(bytes.toString(), Files.readString(log));
    }
    @Test void loggingFailureStillNotifiesUserAndReportsError() throws IOException {
        Path blocker = data.resolve("file"); Files.writeString(blocker, "Keep");
        List<String> messages = new ArrayList<>(); ByteArrayOutputStream errors = new ByteArrayOutputStream();
        new FileLoggingNotifierDecorator(messages::add, blocker.resolve("log"), new PrintStream(errors)).notify("Saved");
        assertEquals(List.of("Saved"), messages);
        assertTrue(errors.toString().contains("Unable to write notification log"));
        assertEquals("Keep", Files.readString(blocker));
    }
}
