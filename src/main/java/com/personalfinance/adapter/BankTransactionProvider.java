package com.personalfinance.adapter;

import com.personalfinance.model.BankImportRecord;
import java.util.List;

public interface BankTransactionProvider { List<BankImportRecord> fetchTransactions(); }
