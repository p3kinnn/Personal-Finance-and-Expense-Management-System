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

public final class BudgetRepository {
    private final CsvFile file;
    public BudgetRepository(Path directory) throws IOException {
        file = new CsvFile(directory.resolve("budgets.csv"), "id,category,year,month,limit");
        findAll();
    }
    public List<Budget> findAll() throws IOException {
        List<Budget> result = new ArrayList<>();
        Set<Long> ids = new HashSet<>();
        Set<String> keys = new HashSet<>();
        int record = 0;
        for (List<String> r : file.read()) {
            record++;
            try {
                Budget item = new Budget(Long.parseLong(r.get(0)), Category.valueOf(r.get(1)),
                        YearMonth.of(Integer.parseInt(r.get(2)), Integer.parseInt(r.get(3))), new BigDecimal(r.get(4)));
                if (!ids.add(item.id())) throw new IllegalArgumentException("Duplicate ID.");
                if (!keys.add(item.category() + ":" + item.month()))
                    throw new IllegalArgumentException("Duplicate category/month budget.");
                result.add(item);
            } catch (RuntimeException ex) { throw file.invalid(record, ex); }
        }
        return result;
    }
    public long nextId() throws IOException {
        return Math.addExact(findAll().stream().mapToLong(Budget::id).max().orElse(0), 1);
    }
    public void saveAll(List<Budget> items) throws IOException {
        // Validate existing data before replacing it, including after an external edit.
        findAll();
        Set<Long> ids = new HashSet<>();
        Set<String> keys = new HashSet<>();
        List<List<String>> rows = new ArrayList<>();
        for (Budget item : items) {
            if (!ids.add(item.id())) throw new IllegalArgumentException("Duplicate ID.");
            if (!keys.add(item.category() + ":" + item.month()))
                    throw new IllegalArgumentException("Duplicate category/month budget.");
            rows.add(List.of(Long.toString(item.id()), item.category().name(), Integer.toString(item.month().getYear()),
                Integer.toString(item.month().getMonthValue()), item.limit().toPlainString()));
        }
        file.write(rows);
    }

}
