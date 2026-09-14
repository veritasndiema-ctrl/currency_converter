package com.mustcc.model;

import java.time.LocalDateTime;

public class Favorite {

    private int favoriteId;
    private CurrencyPair pair;
    private LocalDateTime addedAt;

    public Favorite(int favoriteId, CurrencyPair pair, LocalDateTime addedAt) {
        this.favoriteId = favoriteId;
        this.pair = pair;
        this.addedAt = addedAt;
    }

    public int getFavoriteId() { return favoriteId; }
    public CurrencyPair getPair() { return pair; }
    public LocalDateTime getAddedAt() { return addedAt; }

    @Override
    public String toString() {
        return pair + " (added " + addedAt.toLocalDate() + ")";
    }
}
