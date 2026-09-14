package com.mustcc.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ExchangeRate {

    private int rateId;
    private Currency base;
    private Currency target;
    private Integer sourceId;   // nullable — matches exchange_rates.source_id
    private BigDecimal rate;
    private LocalDate rateDate;

    public ExchangeRate(int rateId, Currency base, Currency target, Integer sourceId,
                         BigDecimal rate, LocalDate rateDate) {
        this.rateId = rateId;
        this.base = base;
        this.target = target;
        this.sourceId = sourceId;
        if (rate == null || rate.signum() <= 0) {
            throw new IllegalArgumentException("Exchange rate must be positive");
        }
        this.rate = rate;
        this.rateDate = rateDate;
    }

    public int getRateId() { return rateId; }
    public Currency getBase() { return base; }
    public Currency getTarget() { return target; }
    public Integer getSourceId() { return sourceId; }
    public BigDecimal getRate() { return rate; }
    public LocalDate getRateDate() { return rateDate; }

    /** Core conversion logic lives with the rate that performs it. */
    public BigDecimal convert(BigDecimal amount) {
        return amount.multiply(rate);
    }

    @Override
    public String toString() {
        return base.getCurrencyCode() + " -> " + target.getCurrencyCode() + " @ " + rate;
    }
}
