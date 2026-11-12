# Domain

> Status: **draft**. The model will evolve.

## Glossary
- **Account**: where money lives (cash, bank, card, wallet). Has a currency and an opening balance.
- **Transaction**: a dated movement of money. Type: `INCOME`, `EXPENSE`, `TRANSFER`.
- **Category**: classifies income or expense (icon and color). Optionally hierarchical.
- **Budget**: spending limit for a category (or overall) per period.
- **Recurring rule**: template that generates transactions on a schedule.

## Data model (draft)
```
Account      (id, name, type, currency, openingBalanceMinor, icon, color, archived)
Category     (id, name, kind[INCOME|EXPENSE], icon, color, parentId?)
Transaction  (id, type, amountMinor:Long>0, accountId, toAccountId?, categoryId?,
              note, dateTimeUtc, createdAt, updatedAt)
Budget       (id, categoryId?, amountMinor, period[MONTHLY|WEEKLY|CUSTOM], startDate)
RecurringRule(id, template fields…, rrule/interval, nextRunAt)
```

## Rules
- `amountMinor` is always positive; the sign comes from `type`.
- Balance = opening + incomes − expenses ± transfers.
- Transfers have no category and don't count toward income or expense totals.
- Format with `NumberFormat.getCurrencyInstance(locale)` plus the currency's fraction digits.

## Later / open questions
- Multi-currency and exchange rates
- Refunds and split transactions
- Attachments (receipts)
- SMS / notification parsing for automatic entry
