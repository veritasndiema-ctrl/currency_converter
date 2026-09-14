package com.mustcc.model;

import java.math.BigDecimal;

/** Wraps a Conversion together with the fee that was deducted from it.
 *  feeApplied is BigDecimal.ZERO when no fee_structures row exists for
 *  the rate source used. */
public class ConversionResult {

    private final Conversion conversion;
    private final BigDecimal feeApplied;

    public ConversionResult(Conversion conversion, BigDecimal feeApplied) {
        this.conversion = conversion;
        this.feeApplied = feeApplied;
    }

    public Conversion getConversion() { return conversion; }
    public BigDecimal getFeeApplied() { return feeApplied; }

    @Override
    public String toString() {
        return conversion + (feeApplied.signum() > 0 ? " (fee deducted: " + feeApplied + ")" : "");
    }
}
