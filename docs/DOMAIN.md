# Domain

> Schema **v3** (v1: M1a, 2026-11-13; v2: required transaction title, 2026-11-15, ADR-021). Code: `model/` (pure Kotlin) and `data/db/` (Room 3). Decisions: ADR-003, ADR-016, ADR-017.

## Glossary
- **Account**: where money lives (cash, bank, card, wallet). It has **one currency** (ISO-4217 code) and an opening balance.
- **Transaction**: a dated movement of money. Its type is `INCOME`, `EXPENSE` or `TRANSFER`. It has a required **title** ("Lunch with team") and an optional **note** for extra detail.
- **Category**: classifies income or expense. It has a `kind` (INCOME/EXPENSE) and an `iconKey` (a Material Symbols name). Users can add, edit and archive it; one no transaction uses can be deleted, and a used one keeps its kind (ADR-040).
- **Budget**: a spending limit per category or overall, per period (M3, not in the schema yet).
- **Recurring rule**: a template that generates transactions (M5, not in the schema yet).

## Models (`model/`)
```
Money(amountMinor: Long, currencyCode: String)          // never Float/Double
Account(id, name, type[CASH|BANK|CARD|WALLET], currencyCode, openingBalance: Money, archived, isDefault)
Category(id, name, kind[INCOME|EXPENSE], iconKey, archived, isSeeded)
Transaction(id, type[INCOME|EXPENSE|TRANSFER], title, amount: Money, accountId, toAccountId?, categoryId?,
            note, occurredAt, createdAt, updatedAt)       // Instants (kotlin.time); title never blank
AccountWithBalance(account, balance: Money, transactionCount)
CategoryWithUsage(category, transactionCount)
PeriodSummary(income: Money, expense: Money)            // net = income − expense; summarize() skips transfers, null if mixed currencies
TransactionListEntry(transaction, account?, toAccount?, category?)  // refs resolved for lists (toListEntries)
```

## Tables (schema v3, `app/schemas/.../3.json`)
| Table | Notes |
|---|---|
| `accounts` | `openingBalanceMinor` INTEGER, enums stored as TEXT |
| `categories` | index on `kind`; `isSeeded` INTEGER (v4, default 0): 1 for the starter set |
| `transactions` | `title` TEXT NOT NULL (v2; never blank in the model), `amountMinor` INTEGER (> 0), `*AtMillis` INTEGER (UTC epoch ms). FKs: `accountId`/`toAccountId` → accounts **RESTRICT** (an account with transactions can't be deleted; archive it instead), `categoryId` → categories **SET NULL**. Indexes on accountId, toAccountId, categoryId, occurredAtMillis |

## Rules (enforced in `Transaction`/`Money` and tested)
- `title` is **required** and never blank (trimmed on save).
- `amount` is always **positive**; direction comes from `type`.
- **Income/Expense**: they need a `categoryId` and have no `toAccountId`.
- **Transfer**: it needs a `toAccountId` that differs from `accountId`, and has no category. It doesn't count toward income/expense totals.
- **Balance** = opening + income − expense − transfers out + transfers in (one SQL query: `AccountDao.observeBalances`).
- **Accounts (ADR-036)**: the opening balance may be negative (a card that starts in debt). The currency isn't shown or picked (ADR-037): new accounts get the device currency. **At most one account is the default** (`isDefault`); it's preselected for new transactions, and archiving clears it. An account can be **deleted only when no transaction refers to it** (FK RESTRICT backs this); otherwise it's archived. Editing the *current* balance reconciles by moving the opening balance: `opening' = entered − (balance − opening)`; no adjustment transaction is created.
- `Money` arithmetic requires the same currency, and overflow throws instead of wrapping.
- Display: `formatMoney(money, locale)` (Android ICU: exact, locale grouping including Indian lakh/crore). **Decimals only when there are any** (ADR-029): "₹200", but "₹50.02" / "₹50.50" (all of the currency's digits, never "₹50.5"). The editor's amount text follows the same rule (`formatMoneyInput`); both use `displayFractionDigits`. Amount text uses `.tabularNumbers`.

## Migrations (ADR-017)
- **v2 → v3** (`Migration2To3`): adds `accounts.isDefault` (0); the oldest active account becomes the default (ADR-037).
- **v3 → v4** (`Migration3To4`): adds `categories.isSeeded` (0); the 13 lowest ids (the starter set, inserted first; categories couldn't be deleted before v4) become 1 (ADR-041). Tested in `MigrationTest`.
- **v1 → v2** (`Migration1To2`, manual): adds `title`. Existing rows get, in order: the category's name, the trimmed note, or the localized type label ("Expense" / "Income" / "Transfer"). Tested in `MigrationTest`.

## Seed data (once, when the database is created; `SeedCallback`)
- Account: **Cash** (type CASH, device-locale currency; INR if the locale has none), the default account.
- Expense categories: Food & dining, Groceries, Transport, Bills & utilities, Shopping, Health, Entertainment, Education, Other expense.
- Income categories: Salary, Business, Gifts, Other income.
- Names come from `strings.xml` at creation time (localizable) and are then normal, editable rows.

## Later / open questions
- Multi-currency totals and exchange rates (the schema already stores a currency per account and transaction)
- Category colors (via M3 color roles; an ADR when needed)
- Refunds and split transactions
- Attachments (receipts)
- SMS / notification parsing (note the Android 17 OTP protection, ADR-008)
