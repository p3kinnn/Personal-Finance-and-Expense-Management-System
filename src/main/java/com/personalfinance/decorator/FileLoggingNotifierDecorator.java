package com.personalfinance.decorator;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public final class FileLoggingNotifierDecorator extends NotifierDecorator {
    private final Path path;
    private final PrintStream errors;
    public FileLoggingNotifierDecorator(Notifier wrapped, Path path, PrintStream errors) {
        super(wrapped); this.path = path; this.errors = errors;
    }
    @Override public void notify(String message) {
        // A log failure must not make a committed transaction appear to have failed.
        try {
            Files.createDirectories(path.toAbsolutePath().getParent());
            Files.writeString(path, message.replace("\r", "\\r").replace("\n", "\\n") + System.lineSeparator(),
                    StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException ex) { errors.println("Unable to write notification log: " + ex.getMessage()); }
        wrapped.notify(message);
    }
}
