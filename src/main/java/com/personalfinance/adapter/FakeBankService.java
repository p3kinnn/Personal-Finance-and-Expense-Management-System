package com.personalfinance.adapter;

import java.util.List;

/** Simulated third-party API; deliberately unaware of the application's models and interfaces. */
public final class FakeBankService {
    public List<ExternalBankRecord> getBankOperations() {
        return List.of(
            new ExternalBankRecord("KZ-202610-001", 25000000, "01/10/2026", "Employer salary", "CREDIT", "SALARY"),
            new ExternalBankRecord("KZ-202610-002", 1250050, "03/10/2026", "Supermarket, weekly groceries", "DEBIT", "FOOD"),
            new ExternalBankRecord("KZ-202610-003", 280000, "05/10/2026", "City transport pass", "DEBIT", "TRANSPORT"),
            new ExternalBankRecord("KZ-202610-004", 1800000, "07/10/2026", "Winter jacket", "DEBIT", "SHOPPING"),
            new ExternalBankRecord("KZ-202610-005", 950000, "10/10/2026", "Electricity bill", "DEBIT", "UTILITIES"),
            new ExternalBankRecord("KZ-202610-006", 1500000, "12/10/2026", "Birthday gift", "CREDIT", "GIFT"));
    }
}
