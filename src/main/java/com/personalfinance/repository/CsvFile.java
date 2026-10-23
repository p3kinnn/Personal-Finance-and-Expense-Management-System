package com.personalfinance.repository;

import com.personalfinance.util.Csv;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

final class CsvFile {
    private final Path path;
    private final List<String> header;
    CsvFile(Path path, String header) throws IOException {
        this.path = path;
        this.header = List.of(header.split(","));
        if (!Files.exists(path)) Csv.write(path, List.of(this.header));
    }
    List<List<String>> read() throws IOException {
        if (!Files.exists(path)) Csv.write(path, List.of(header));
        List<List<String>> rows;
        try { rows = Csv.parse(Files.readString(path, StandardCharsets.UTF_8)); }
        catch (IOException ex) { throw new IOException(path + ": " + ex.getMessage(), ex); }
        if (rows.isEmpty() || !rows.get(0).equals(header))
            throw new IOException(path + ": invalid or missing CSV header; original file preserved.");
        for (int i = 1; i < rows.size(); i++)
            if (rows.get(i).size() != header.size()) throw invalid(i, null);
        return rows.subList(1, rows.size());
    }
    IOException invalid(int record, Exception cause) {
        return new IOException(path + ": corrupted CSV record " + (record + 1) + "; original file preserved.", cause);
    }
    void write(List<List<String>> records) throws IOException {
        List<List<String>> rows = new ArrayList<>(); rows.add(header); rows.addAll(records);
        Csv.write(path, rows);
    }
}
