package com.personalfinance.adapter;

import com.personalfinance.model.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;

public final class BankAdapter implements BankTransactionProvider {
    private final FakeBankService bank;
    public BankAdapter(FakeBankService bank) { this.bank = bank; }
    @Override public List<BankImportRecord> fetchTransactions() {
        List<BankImportRecord> result = new ArrayList<>();
        for (ExternalBankRecord record : bank.getBankOperations()) result.add(convert(record));
        return result;
    }
    public BankImportRecord convert(ExternalBankRecord record) {
        TransactionType type = switch (record.operationDirection()) {
            case "CREDIT" -> TransactionType.INCOME;
            case "DEBIT" -> TransactionType.EXPENSE;
            default -> throw new IllegalArgumentException("Unknown bank operation direction.");
        };
        Category category;
        try { category = Category.valueOf(record.merchantGroup()); }
        catch (IllegalArgumentException ex) { category = type == TransactionType.INCOME ? Category.OTHER_INCOME : Category.OTHER_EXPENSE; }
        if (category.type() != type) category = type == TransactionType.INCOME ? Category.OTHER_INCOME : Category.OTHER_EXPENSE;
        return new BankImportRecord(type, BigDecimal.valueOf(record.operationAmountInCents(), 2), category,
                record.operationDescription(), LocalDate.parse(record.operationDate(),
                DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT)), record.operationCode());
    }
}
