# Changelog

All notable changes to Fiscus are listed here. The format follows
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and versions follow
[Semantic Versioning](https://semver.org/).

## [0.1.0-beta.2]

### Added

- **Your profile**: add your name and a profile photo in Settings → Profile. The photo
  is picked with Android's photo picker (no permissions needed) and stays on your phone.
- **A personal greeting** on Home: "Good morning", "Good afternoon", "Good evening" or
  "Good night" with your name, and your profile photo in the top corner.
- **Opening balance** for accounts: set how much an account held before you started
  using Fiscus. Start it with "−" for money already owed, such as a credit card.

### Changed

- **New app icon**, also shown on the splash screen.
- The splash screen now takes its background from your wallpaper colours on Android 12
  and newer.
- With Dynamic color turned off, Fiscus now uses a clean black-and-white (monochrome)
  theme.
- A refreshed Material 3 Expressive look: softer rounded icons, buttons that change
  shape when pressed, and Edit as a floating button on a transaction's details.
- Account cards on Home are always shown full size, so names and balances are never cut
  off.
- Home shows a friendlier message when you have no transactions yet.

### Fixed

- The ready-made categories and Cash account were missing when Android restored an
  earlier backup during install.

### Known limitations

- English only, with amounts in your phone's currency.
- There's no export or backup inside the app yet. Android's own device backup may
  restore your data on a new phone.
- Unsaved transaction drafts are lost if Android closes the app in the background.
- The Home screen's month doesn't change at midnight on the 1st until you reopen it.

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

[0.1.0-beta.2]: https://github.com/prajwalpawar7744/fiscus/compare/v0.1.0-beta.1...v0.1.0-beta.2
[0.1.0-beta.1]: https://github.com/prajwalpawar7744/fiscus/releases/tag/v0.1.0-beta.1
