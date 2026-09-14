package com.mustcc.service;

import com.mustcc.dao.CurrencyDAO;
import com.mustcc.dao.RateHistoryDAO;
import com.mustcc.exception.InvalidCurrencyException;
import com.mustcc.model.Currency;
import com.mustcc.model.RateHistoryEntry;

import java.util.List;

public class RateHistoryService {

    private final CurrencyDAO currencyDAO = new CurrencyDAO();
    private final RateHistoryDAO rateHistoryDAO = new RateHistoryDAO();

    public List<RateHistoryEntry> recentHistory(String baseCode, String targetCode, int days)
            throws InvalidCurrencyException {
        Currency base = currencyDAO.findByCode(baseCode).orElseThrow(() -> new InvalidCurrencyException(baseCode));
        Currency target = currencyDAO.findByCode(targetCode).orElseThrow(() -> new InvalidCurrencyException(targetCode));
        return rateHistoryDAO.findRecent(base.getCurrencyId(), target.getCurrencyId(), days);
    }
}
