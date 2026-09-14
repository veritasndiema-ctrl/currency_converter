package com.mustcc.dao;

import java.util.List;
import java.util.Optional;

/**
 * Generic CRUD contract every DAO implements. Using a generic
 * interface here is what lets every concrete DAO share a common
 * shape while still returning its own model type — a clean way to
 * show interfaces + generics + polymorphism together.
 *
 * @param <T>  the model type (Currency, ExchangeRate, ...)
 * @param <ID> the primary key type (usually Integer)
 */
public interface Repository<T, ID> {
    Optional<T> findById(ID id);
    List<T> findAll();
    T save(T entity);
    void deleteById(ID id);
}
