package com.personalfinance.service;

import com.personalfinance.decorator.Notifier;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class NotificationService {
    private final Notifier normal;
    private final Notifier important;
    private final Path log;
    public NotificationService(Notifier normal, Notifier important, Path log) throws IOException {
        this.normal = normal; this.important = important; this.log = log;
        Files.createDirectories(log.toAbsolutePath().getParent());
        if (!Files.exists(log)) Files.createFile(log);
    }
    public void info(String message) { normal.notify(message); }
    public void important(String message) { important.notify(message); }
    public String history() throws IOException { return Files.readString(log, StandardCharsets.UTF_8); }
}
