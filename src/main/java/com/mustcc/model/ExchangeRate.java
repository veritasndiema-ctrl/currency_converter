package com.mustcc.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ExchangeRate {

    private int rateId;
    private Currency base;
    private Currency target;
    private BigDecimal rate;
    private LocalDate rateDate;

    public ExchangeRate(int rateId, Currency base, Currency target, BigDecimal rate, LocalDate rateDate) {
        this.rateId = rateId;
        this.base = base;
        this.target = target;
        if (rate == null || rate.signum() <= 0) {
            throw new IllegalArgumentException("Exchange rate must be positive");
        }
        this.rate = rate;
        this.rateDate = rateDate;
    }

    public int getRateId() { return rateId; }
    public Currency getBase() { return base; }
    public Currency getTarget() { return target; }
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
