# Currency Converter — OOP II + Database Programming

## Setup

1. **Database**: open `sql/schema.sql` in MySQL Workbench (or `mysql -u root < sql/schema.sql`)
   and run it. It creates the `currency_converter` database, all 16 tables, and seeds
   realistic demo data — 8 currencies, 9 pairs with rates, a few days of rate history,
   a fee structure, and one demo login (`demo_user` / `Demo@1234`).

2. **Configure your DB credentials**: copy `src/main/resources/db.properties.example`
   to `src/main/resources/db.properties` (same folder) and fill in your own MySQL
   username/password. `db.properties` is gitignored on purpose — never commit real
   credentials.

3. **Build**: this is now a real Maven project.
   ```
   mvn clean package
   ```
   This pulls in the MySQL Connector/J driver and bcrypt (for password hashing)
   automatically, runs any tests, and produces a runnable fat jar at
   `target/currency-converter.jar`.

4. **Run**:
   ```
   java -jar target/currency-converter.jar
   ```
   You'll land on a menu: log in (try the seeded `demo_user` / `Demo@1234`), register
   a new account, or continue as a guest. From there, a menu drives everything —
   conversions, fee-aware conversions, rate history, favorites, and (if logged in)
   your own conversion history.

## Structure

```
model/      Currency, ExchangeRate, User, Conversion, CurrencyPair, Favorite,
            RateHistoryEntry, FeeStructure, ConversionResult — plain domain objects
dao/        Repository<T,ID> interface, BaseDAO, + one DAO per table actually wired up:
            CurrencyDAO, ExchangeRateDAO, ConversionDAO, UserDAO, RoleDAO, SessionDAO,
            CurrencyPairDAO, FavoriteDAO, RateHistoryDAO, FeeStructureDAO,
            AuditTrailDAO, TransactionLogDAO, ErrorLogDAO
service/    ConversionService, AuthService, FavoritesService, RateHistoryService —
            the only classes that talk to multiple DAOs at once
exception/  DataAccessException, InvalidCurrencyException, RateUnavailableException,
            AuthException
util/       DatabaseConnection — singleton JDBC connection holder, reads db.properties
Main.java   Menu-driven CLI entry point
```

Layering is deliberate: `UI (Main) -> Service -> DAO -> Model`. Nothing above the
service layer touches JDBC directly.

## What's implemented

13 of the 16 tables now have a working DAO and are exercised by the CLI:

- **Accounts & sessions** — register/login (bcrypt-hashed passwords), `user_sessions`
  opened on login and closed on logout.
- **Conversions** — the original flow, now also writing to `rate_history`,
  `audit_trail`, and `transaction_log` on every success, and `error_log` on failure.
- **Fee-aware conversions** — looks up `fee_structures` for the rate's source and
  shows the fee-adjusted amount received.
- **Rate history** — last-30-days lookup for any pair, built from the append-only
  `rate_history` log.
- **Favorites** — add/remove/list favorite currency pairs per user, backed by
  `currency_pairs` + `favorites`.
- **Conversion history** — a logged-in user can list their own past conversions.

Not wired up (left as a pattern to follow, same as before): `alerts` — the schema
supports rate-target alerts, but no DAO/service exists yet. Follow the same
`Repository<T,ID>` + `BaseDAO` shape used everywhere else if you want to add it.

## Verification

This was compiled clean with `javac` against the real API surface of every
dependency (including a real run of the bcrypt hashing/verification logic), and
the full schema plus seed data was run end-to-end against a live MySQL-compatible
server — registration, login (including a rejected wrong-password attempt),
conversions, fee calculation, favorites, rate history, and conversion history were
all exercised and the resulting rows in `audit_trail`, `transaction_log`,
`error_log`, and `user_sessions` were checked directly. `mvn clean package` itself
wasn't run in the sandbox this was built in (no access to Maven Central to resolve
dependencies there) — run it once on your machine as a final check, but the logic
underneath it has already been validated.

## ERD & normalization

See `docs/ERD.md` for the entity-relationship diagram (Mermaid — renders on GitHub)
and the full 1NF → 2NF write-up, plus an explicit justification for the two places
(`conversions.rate_used`/`converted_amount` and `rate_history`) where the schema
keeps derived data on purpose for audit reasons rather than chasing 3NF further.
