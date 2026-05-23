package com.syfe.finance_manager.dto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;

public class YearlyReportResponse {
    private int year;
    private Map<String, BigDecimal> totalIncome = new LinkedHashMap<>();
    private Map<String, BigDecimal> totalExpenses = new LinkedHashMap<>();
    private BigDecimal netSavings;

    public YearlyReportResponse() {}

    public YearlyReportResponse(int year, Map<String, BigDecimal> totalIncome, 
                                Map<String, BigDecimal> totalExpenses, BigDecimal netSavings) {
        this.year = year;
        
        if (totalIncome != null) {
            totalIncome.forEach((k, v) -> this.totalIncome.put(k, formatBigDecimal(v)));
        }
        if (totalExpenses != null) {
            totalExpenses.forEach((k, v) -> this.totalExpenses.put(k, formatBigDecimal(v)));
        }
        this.netSavings = formatBigDecimal(netSavings);
    }

    private BigDecimal formatBigDecimal(BigDecimal value) {
        if (value == null) {
            return null;
        }
        if (value.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO.setScale(0); // Serializes as 0
        }
        return value.setScale(2, RoundingMode.HALF_UP); // Serializes as XXXX.00
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public Map<String, BigDecimal> getTotalIncome() {
        return totalIncome;
    }

    public void setTotalIncome(Map<String, BigDecimal> totalIncome) {
        this.totalIncome = new LinkedHashMap<>();
        if (totalIncome != null) {
            totalIncome.forEach((k, v) -> this.totalIncome.put(k, formatBigDecimal(v)));
        }
    }

    public Map<String, BigDecimal> getTotalExpenses() {
        return totalExpenses;
    }

    public void setTotalExpenses(Map<String, BigDecimal> totalExpenses) {
        this.totalExpenses = new LinkedHashMap<>();
        if (totalExpenses != null) {
            totalExpenses.forEach((k, v) -> this.totalExpenses.put(k, formatBigDecimal(v)));
        }
    }

    public BigDecimal getNetSavings() {
        return netSavings;
    }

    public void setNetSavings(BigDecimal netSavings) {
        this.netSavings = formatBigDecimal(netSavings);
    }
}
