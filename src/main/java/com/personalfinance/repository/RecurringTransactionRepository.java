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

public final class RecurringTransactionRepository {
    private final CsvFile file;
    public RecurringTransactionRepository(Path directory) throws IOException {
        file = new CsvFile(directory.resolve("recurring_transactions.csv"), "id,type,amount,category,description,startDate,active,lastProcessedMonth");
        findAll();
    }
    public List<RecurringTransaction> findAll() throws IOException {
        List<RecurringTransaction> result = new ArrayList<>();
        Set<Long> ids = new HashSet<>();
        int record = 0;
        for (List<String> r : file.read()) {
            record++;
            try {
                RecurringTransaction item = new RecurringTransaction(Long.parseLong(r.get(0)), TransactionType.valueOf(r.get(1)),
                        new BigDecimal(r.get(2)), Category.valueOf(r.get(3)), r.get(4), LocalDate.parse(r.get(5)),
                        parseActive(r.get(6)), r.get(7).isEmpty() ? null : YearMonth.parse(r.get(7)));
                if (!ids.add(item.id())) throw new IllegalArgumentException("Duplicate ID.");

                result.add(item);
            } catch (RuntimeException ex) { throw file.invalid(record, ex); }
        }
        return result;
    }
    public long nextId() throws IOException {
        return Math.addExact(findAll().stream().mapToLong(RecurringTransaction::id).max().orElse(0), 1);
    }
    public void saveAll(List<RecurringTransaction> items) throws IOException {
        // Validate existing data before replacing it, including after an external edit.
        findAll();
        Set<Long> ids = new HashSet<>();
        List<List<String>> rows = new ArrayList<>();
        for (RecurringTransaction item : items) {
            if (!ids.add(item.id())) throw new IllegalArgumentException("Duplicate ID.");

            rows.add(List.of(Long.toString(item.id()), item.type().name(), item.amount().toPlainString(),
                item.category().name(), item.description(), item.startDate().toString(), Boolean.toString(item.active()),
                item.lastProcessedMonth() == null ? "" : item.lastProcessedMonth().toString()));
        }
        file.write(rows);
    }
    private static boolean parseActive(String value) {
        if (!value.equals("true") && !value.equals("false")) throw new IllegalArgumentException("Invalid active flag.");
        return Boolean.parseBoolean(value);
    }
}
