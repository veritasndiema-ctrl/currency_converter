package com.mustcc.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Conversion {

    private int conversionId;
    private User user;               // nullable — guest conversions allowed
    private Currency base;
    private Currency target;
    private BigDecimal amount;
    private BigDecimal convertedAmount;
    private BigDecimal rateUsed;
    private LocalDateTime conversionDate;

    public Conversion(User user, Currency base, Currency target,
                       BigDecimal amount, BigDecimal convertedAmount, BigDecimal rateUsed) {
        this.user = user;
        this.base = base;
        this.target = target;
        this.amount = amount;
        this.convertedAmount = convertedAmount;
        this.rateUsed = rateUsed;
        this.conversionDate = LocalDateTime.now();
    }

    public int getConversionId() { return conversionId; }
    public void setConversionId(int conversionId) { this.conversionId = conversionId; }
    public User getUser() { return user; }
    public Currency getBase() { return base; }
    public Currency getTarget() { return target; }
    public BigDecimal getAmount() { return amount; }
    public BigDecimal getConvertedAmount() { return convertedAmount; }
    public BigDecimal getRateUsed() { return rateUsed; }
    public LocalDateTime getConversionDate() { return conversionDate; }

    @Override
    public String toString() {
        return String.format("%s %s -> %s %s (rate %s)",
                amount, base.getCurrencyCode(), convertedAmount, target.getCurrencyCode(), rateUsed);
    }
}
