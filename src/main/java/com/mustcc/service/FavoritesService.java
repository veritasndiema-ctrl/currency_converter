package com.mustcc.service;

import com.mustcc.dao.CurrencyDAO;
import com.mustcc.dao.CurrencyPairDAO;
import com.mustcc.dao.FavoriteDAO;
import com.mustcc.exception.InvalidCurrencyException;
import com.mustcc.model.Currency;
import com.mustcc.model.Favorite;
import com.mustcc.model.User;

import java.util.List;

public class FavoritesService {

    private final CurrencyDAO currencyDAO = new CurrencyDAO();
    private final CurrencyPairDAO pairDAO = new CurrencyPairDAO();
    private final FavoriteDAO favoriteDAO = new FavoriteDAO();

    public void addFavorite(User user, String baseCode, String targetCode) throws InvalidCurrencyException {
        Currency base = resolve(baseCode);
        Currency target = resolve(targetCode);
        var pair = pairDAO.findOrCreate(base.getCurrencyId(), target.getCurrencyId());
        favoriteDAO.add(user.getUserId(), pair.getPairId());
    }

    public void removeFavorite(User user, String baseCode, String targetCode) throws InvalidCurrencyException {
        Currency base = resolve(baseCode);
        Currency target = resolve(targetCode);
        var pair = pairDAO.findOrCreate(base.getCurrencyId(), target.getCurrencyId());
        favoriteDAO.remove(user.getUserId(), pair.getPairId());
    }

    public List<Favorite> listFavorites(User user) {
        return favoriteDAO.listForUser(user.getUserId());
    }

    private Currency resolve(String code) throws InvalidCurrencyException {
        return currencyDAO.findByCode(code).orElseThrow(() -> new InvalidCurrencyException(code));
    }
}
