package com.mustcc.model;

import java.math.BigDecimal;

public class FeeStructure {

    private int feeId;
    private int sourceId;
    private BigDecimal feePercentage;
    private BigDecimal minFee;
    private BigDecimal maxFee;   // nullable — no cap if null

    public FeeStructure(int feeId, int sourceId, BigDecimal feePercentage,
                         BigDecimal minFee, BigDecimal maxFee) {
        this.feeId = feeId;
        this.sourceId = sourceId;
        this.feePercentage = feePercentage;
        this.minFee = minFee;
        this.maxFee = maxFee;
    }

    public int getFeeId() { return feeId; }
    public int getSourceId() { return sourceId; }
    public BigDecimal getFeePercentage() { return feePercentage; }
    public BigDecimal getMinFee() { return minFee; }
    public BigDecimal getMaxFee() { return maxFee; }

    /** Applies feePercentage to convertedAmount, then clamps to [minFee, maxFee]. */
    public BigDecimal calculateFee(BigDecimal convertedAmount) {
        BigDecimal fee = convertedAmount
                .multiply(feePercentage)
                .divide(BigDecimal.valueOf(100));

        if (minFee != null && fee.compareTo(minFee) < 0) {
            fee = minFee;
        }
        if (maxFee != null && fee.compareTo(maxFee) > 0) {
            fee = maxFee;
        }
        return fee;
    }
}
