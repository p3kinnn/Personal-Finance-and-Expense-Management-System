package com.personalfinance.util;

import java.math.BigDecimal;

public final class Money {
    public static final BigDecimal ZERO = new BigDecimal("0.00");
    private Money() { }
    public static BigDecimal positive(BigDecimal value) {
        if (value == null || value.signum() <= 0 || value.scale() > 2)
            throw new IllegalArgumentException("Invalid amount. Enter a positive number with at most two decimal places.");
        return value.setScale(2);
    }
    public static BigDecimal parse(String text) {
        if (text == null || !text.strip().matches("[0-9]+(\\.[0-9]{1,2})?"))
            throw new IllegalArgumentException("Invalid amount. Enter a positive number with at most two decimal places.");
        return positive(new BigDecimal(text.strip()));
    }
    public static String format(BigDecimal value) { return value.setScale(2).toPlainString() + " KZT"; }
}
