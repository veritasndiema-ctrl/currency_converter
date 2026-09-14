package com.mustcc.model;

/** A base->target currency pair, e.g. USD -> UGX. Backs favorites and alerts,
 *  which reference a pair rather than duplicating two currency codes each. */
public class CurrencyPair {

    private int pairId;
    private Currency base;
    private Currency target;
    private boolean active;

    public CurrencyPair(int pairId, Currency base, Currency target, boolean active) {
        this.pairId = pairId;
        this.base = base;
        this.target = target;
        this.active = active;
    }

    public int getPairId() { return pairId; }
    public Currency getBase() { return base; }
    public Currency getTarget() { return target; }
    public boolean isActive() { return active; }

    @Override
    public String toString() {
        return base.getCurrencyCode() + " -> " + target.getCurrencyCode();
    }
}
