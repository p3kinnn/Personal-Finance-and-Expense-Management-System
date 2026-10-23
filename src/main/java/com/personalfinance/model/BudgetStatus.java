package com.personalfinance.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record BudgetStatus(Budget budget, BigDecimal spent) {
    public BigDecimal remaining() { return budget.limit().subtract(spent); }
    public BigDecimal usage() {
        return spent.multiply(new BigDecimal("100")).divide(budget.limit(), 2, RoundingMode.HALF_UP);
    }
    public String level() {
        int comparison = spent.compareTo(budget.limit());
        if (comparison > 0) return "Budget exceeded";
        if (comparison == 0) return "Budget limit reached";
        if (spent.compareTo(budget.limit().multiply(new BigDecimal("0.80"))) >= 0)
            return "Budget approaching limit";
        return "Within budget";
    }
}
