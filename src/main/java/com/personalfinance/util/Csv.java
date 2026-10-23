package com.personalfinance.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/** Small CSV codec supporting quoted commas, quotes, CRLF and multiline fields. */
public final class Csv {
    private Csv() { }
    public static String row(List<String> fields) {
        return fields.stream().map(Csv::escape).collect(java.util.stream.Collectors.joining(","));
    }
    private static String escape(String field) {
        if (field.contains(",") || field.contains("\"") || field.contains("\n") || field.contains("\r"))
            return "\"" + field.replace("\"", "\"\"") + "\"";
        return field;
    }
    public static List<List<String>> parse(String text) throws IOException {
        List<List<String>> rows = new ArrayList<>();
        List<String> fields = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean quoted = false;
        boolean closedQuote = false;
        boolean started = false;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (quoted) {
                if (c == '"') {
                    if (i + 1 < text.length() && text.charAt(i + 1) == '"') { field.append('"'); i++; }
                    else { quoted = false; closedQuote = true; }
                } else field.append(c);
                continue;
            }
            if (c == ',' || c == '\n' || c == '\r') {
                fields.add(field.toString()); field.setLength(0);
                closedQuote = false; started = false;
                if (c != ',') {
                    rows.add(List.copyOf(fields)); fields.clear();
                    if (c == '\r' && i + 1 < text.length() && text.charAt(i + 1) == '\n') i++;
                }
            } else {
                if (closedQuote) throw new IOException("Unexpected character after closing CSV quote.");
                if (c == '"') {
                    if (started) throw new IOException("Quote inside an unquoted CSV field.");
                    quoted = true;
                } else field.append(c);
                started = true;
            }
        }
        if (quoted) throw new IOException("Unclosed CSV quote.");
        if (started || closedQuote || !fields.isEmpty()) {
            fields.add(field.toString()); rows.add(List.copyOf(fields));
        }
        return rows;
    }
    public static void write(Path path, List<List<String>> rows) throws IOException {
        Path target = path.toAbsolutePath();
        Files.createDirectories(target.getParent());
        Path temporary = Files.createTempFile(target.getParent(), ".finance-", ".tmp");
        try {
            StringBuilder content = new StringBuilder();
            for (List<String> fields : rows) content.append(row(fields)).append('\n');
            Files.writeString(temporary, content, StandardCharsets.UTF_8);
            try { Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING); }
            catch (AtomicMoveNotSupportedException ex) {
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally { Files.deleteIfExists(temporary); }
    }
}
