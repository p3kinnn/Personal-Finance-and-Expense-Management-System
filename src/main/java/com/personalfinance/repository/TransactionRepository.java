package com.personalfinance.repository;

import com.personalfinance.model.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class TransactionRepository {
    private final CsvFile file;
    public TransactionRepository(Path directory) throws IOException {
        file = new CsvFile(directory.resolve("transactions.csv"), "id,type,amount,category,description,date,source,externalId");
        findAll();
    }
    public List<Transaction> findAll() throws IOException {
        List<Transaction> result = new ArrayList<>();
        Set<Long> ids = new HashSet<>();
        Set<String> keys = new HashSet<>();
        int record = 0;
        for (List<String> r : file.read()) {
            record++;
            try {
                Transaction item = new Transaction(Long.parseLong(r.get(0)), TransactionType.valueOf(r.get(1)),
                        new BigDecimal(r.get(2)), Category.valueOf(r.get(3)), r.get(4),
                        LocalDate.parse(r.get(5)), TransactionSource.valueOf(r.get(6)), r.get(7));
                if (!ids.add(item.id())) throw new IllegalArgumentException("Duplicate ID.");
                if (!item.externalId().isEmpty() && !keys.add(item.source() + ":" + item.externalId()))
                    throw new IllegalArgumentException("Duplicate external transaction ID.");
                result.add(item);
            } catch (RuntimeException ex) { throw file.invalid(record, ex); }
        }
        return result;
    }
    public long nextId() throws IOException {
        return Math.addExact(findAll().stream().mapToLong(Transaction::id).max().orElse(0), 1);
    }
    public void saveAll(List<Transaction> items) throws IOException {
        // Validate existing data before replacing it, including after an external edit.
        findAll();
        Set<Long> ids = new HashSet<>();
        Set<String> keys = new HashSet<>();
        List<List<String>> rows = new ArrayList<>();
        for (Transaction item : items) {
            if (!ids.add(item.id())) throw new IllegalArgumentException("Duplicate ID.");
            if (!item.externalId().isEmpty() && !keys.add(item.source() + ":" + item.externalId()))
                    throw new IllegalArgumentException("Duplicate external transaction ID.");
            rows.add(List.of(Long.toString(item.id()), item.type().name(), item.amount().toPlainString(),
                item.category().name(), item.description(), item.date().toString(), item.source().name(), item.externalId()));
        }
        file.write(rows);
    }

}
