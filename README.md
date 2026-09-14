# Currency Converter — OOP II + Database Programming

## Setup

1. **Database**: open `sql/schema.sql` in MySQL Workbench and run it. It creates the
   `currency_converter` database, all 16 tables, and seeds a few rows (UGX/USD/GBP/KES)
   so you can test conversions immediately.

2. **Java project**: this is a plain Maven-style source layout
   (`src/main/java/com/mustcc/...`). Add these to your project:
   - JDK 17+ (or whatever your course requires)
   - **MySQL Connector/J** on the classpath (download the jar, or add as a Maven/Gradle
     dependency: `mysql:mysql-connector-j:8.x`) — needed at runtime for the `jdbc:mysql://`
     URL to resolve, even though the source code only imports `java.sql.*`.

3. Edit `DatabaseConnection.java` with your actual MySQL username/password.

4. Run `Main.java` — it's a small CLI: enter a from-currency, to-currency, and amount.

## Structure

```
model/      Currency, ExchangeRate, User, Conversion  — plain domain objects
dao/        Repository<T,ID> interface, BaseDAO, CurrencyDAO, ExchangeRateDAO, ConversionDAO
service/    ConversionService — the only class that talks to multiple DAOs at once
exception/  DataAccessException, InvalidCurrencyException, RateUnavailableException
util/       DatabaseConnection — singleton JDBC connection holder
Main.java   CLI entry point
```

Layering is deliberate: `UI (Main) -> Service -> DAO -> Model`. Nothing above the
service layer touches JDBC directly.

## What's implemented vs. what's a pattern for you to repeat

Fully implemented: `Currency`, `ExchangeRate`, `Conversion` DAOs and the conversion flow
end-to-end. The schema has 16 tables (`favorites`, `alerts`, `audit_trail`,
`transaction_log`, `user_sessions`, `rate_history`, `fee_structures`, `error_log`, etc.)
that aren't wired into DAOs yet — that's intentional. Follow the same
`Repository<T,ID>` + `BaseDAO` pattern used in `CurrencyDAO` to add DAOs for those as
you build out features (favorites list, rate alerts, audit logging). Same shape every
time: extend `BaseDAO`, implement `Repository<T, Integer>`, write the four methods with
`PreparedStatement`.

## Note on verification

This was written and manually reviewed for correctness (matching constructor
signatures, imports, and method calls across files) but not compiled in a live JDK —
the sandbox this was built in doesn't have `javac`/network access to install one.
Double check it compiles on your machine before you build on top of it, and add
the MySQL driver jar first (that's the most likely first error otherwise).

## 2NF note (for your write-up)

See the comment block at the top of `schema.sql`. Short version: every table uses a
surrogate integer primary key, so 2NF partial-dependency issues can only arise on
tables with a *natural* composite key — `currency_pairs`, `exchange_rates`, and
`rate_history` — where the composite is enforced as a `UNIQUE` constraint instead of
being the primary key. That keeps every non-key column dependent on the whole key by
construction.
