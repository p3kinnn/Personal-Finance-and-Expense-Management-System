package com.personalfinance.service;

import com.personalfinance.adapter.BankTransactionProvider;
import com.personalfinance.model.*;
import java.io.IOException;

public final class BankImportService {
    public record ImportResult(int imported, int skipped) { }
    private final BankTransactionProvider provider;
    private final TransactionService transactions;
    private final NotificationService notifications;
    public BankImportService(BankTransactionProvider provider, TransactionService transactions, NotificationService notifications) {
        this.provider = provider; this.transactions = transactions; this.notifications = notifications;
    }
    public ImportResult importTransactions() throws IOException {
        int imported = 0;
        int skipped = 0;
        for (BankImportRecord record : provider.fetchTransactions()) {
            if (transactions.hasExternalId(TransactionSource.BANK, record.externalId())) { skipped++; continue; }
            transactions.add(record.type(), record.amount(), record.category(), record.description(), record.date(), TransactionSource.BANK, record.externalId());
            imported++;
        }
        notifications.info(imported + " bank transactions imported. " + skipped + " duplicates skipped.");
        return new ImportResult(imported, skipped);
    }
}
