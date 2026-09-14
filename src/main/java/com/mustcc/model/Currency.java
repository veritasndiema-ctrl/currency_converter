package com.mustcc.model;

import java.util.Objects;

/** Immutable-ish domain model for a currency. Encapsulation: fields
 *  are private, mutated only through explicit setters, validated on
 *  construction. */
public class Currency {

    private int currencyId;
    private String currencyCode;   // ISO 4217, e.g. "USD"
    private String currencyName;
    private String symbol;

    public Currency(int currencyId, String currencyCode, String currencyName, String symbol) {
        this.currencyId = currencyId;
        this.currencyCode = validateCode(currencyCode);
        this.currencyName = currencyName;
        this.symbol = symbol;
    }

    private String validateCode(String code) {
        if (code == null || code.length() != 3) {
            throw new IllegalArgumentException("Currency code must be exactly 3 letters: " + code);
        }
        return code.toUpperCase();
    }

    public int getCurrencyId() { return currencyId; }
    public String getCurrencyCode() { return currencyCode; }
    public String getCurrencyName() { return currencyName; }
    public String getSymbol() { return symbol; }

    public void setCurrencyName(String currencyName) { this.currencyName = currencyName; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Currency)) return false;
        Currency currency = (Currency) o;
        return currencyId == currency.currencyId;
    }

    @Override
    public int hashCode() { return Objects.hash(currencyId); }

    @Override
    public String toString() {
        return currencyCode + " (" + currencyName + ")";
    }
}
