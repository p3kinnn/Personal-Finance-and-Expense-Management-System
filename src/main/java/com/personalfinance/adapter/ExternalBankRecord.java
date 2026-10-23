package com.personalfinance.adapter;


public record ExternalBankRecord(String operationCode, long operationAmountInCents,
        String operationDate, String operationDescription, String operationDirection, String merchantGroup) { }
