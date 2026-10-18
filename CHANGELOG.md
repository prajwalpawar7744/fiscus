# Changelog

All notable changes to Fiscus are listed here. The format follows
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and versions follow
[Semantic Versioning](https://semver.org/).

## [0.1.0-beta.1] - 2026-10-18

The first public beta. Fiscus is a personal finance manager designed with
Material 3 Expressive. Everything stays on your device: the app has no internet
access and no account.

### Added

**Recording money**
- Add income, expenses and transfers between accounts from the "Add transaction" button.
- Every transaction has an amount, a title, a category (or From/To accounts for a
  transfer), an account, a date and an optional note.
- Your default account is picked for you, and a half-filled transaction is kept if you
  close the sheet by accident.
- Create a new category or account straight from the transaction form.

**Home**
- Total balance across your accounts. Credit card spending counts as money owed.
- This month's income and spending, with a wavy bar showing how much of your income
  you've spent.
- A carousel of your accounts and their balances.
- Your 10 most recent transactions, grouped by day.

**Transactions**
- Browse any month, a single day, or any date range: tap the date to pick a range,
  or step back and forward with the arrows.
- Filter by type (expense, income, transfer), account and category. The totals
  follow your filters.
- Search titles and notes across all your history.
- Open any transaction to see its details, then edit or delete it.

**Analysis**
- Spent, change versus the previous period, daily average and savings rate for any
  period you pick.
- Where your money went (or came from), by category, as a share of the total.
- A spending trend by day or by month, depending on the period.
- Your biggest expenses for the period.

**Categories and accounts**
- 14 ready-made categories and a Cash account to start with.
- Add, rename, change the icon of, archive or restore categories and accounts from
  Settings → Manage. Anything never used can be deleted.

**Personalisation**
- Light, dark or system theme, and colours taken from your wallpaper on
  Android 12 and newer.
- Choose the shape used for icons (circle, square, cookie, clover, sunny or flower).
- Choose the app bar size, how it behaves when you scroll, and how the bottom bar
  shows labels.

### Known limitations

- English only, with amounts in your phone's currency.
- An account's starting balance can't be set yet; every new account starts at zero.
- There's no export or backup inside the app yet. Android's own device backup may
  restore your data on a new phone.
- Unsaved transaction drafts are lost if Android closes the app in the background.
- The Home screen's month doesn't change at midnight on the 1st until you reopen it.

[0.1.0-beta.1]: https://github.com/prajwalpawar7744/fiscus/releases/tag/v0.1.0-beta.1
