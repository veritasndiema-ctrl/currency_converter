package com.mustcc.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** A single snapshot of a pair's rate at a point in time, used for the
 *  "rate history" lookup feature. Distinct from ExchangeRate: exchange_rates
 *  holds one row per pair per day (the rate in force that day), while
 *  rate_history is an append-only log of every rate observed. */
public class RateHistoryEntry {

    private int historyId;
    private Currency base;
    private Currency target;
    private BigDecimal rate;
    private LocalDateTime recordedAt;

    public RateHistoryEntry(int historyId, Currency base, Currency target,
                             BigDecimal rate, LocalDateTime recordedAt) {
        this.historyId = historyId;
        this.base = base;
        this.target = target;
        this.rate = rate;
        this.recordedAt = recordedAt;
    }

    public int getHistoryId() { return historyId; }
    public Currency getBase() { return base; }
    public Currency getTarget() { return target; }
    public BigDecimal getRate() { return rate; }
    public LocalDateTime getRecordedAt() { return recordedAt; }

    @Override
    public String toString() {
        return recordedAt + "  " + base.getCurrencyCode() + "->" + target.getCurrencyCode() + " @ " + rate;
    }
}
