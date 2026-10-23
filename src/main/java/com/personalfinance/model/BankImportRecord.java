package com.personalfinance.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BankImportRecord(TransactionType type, BigDecimal amount, Category category,
                               String description, LocalDate date, String externalId) { }
