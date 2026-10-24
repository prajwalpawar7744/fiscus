# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- Default account switch in the account editor (Settings → Manage → Accounts): new transactions start with the chosen account.
- Tapping the total balance on Home opens the accounts list.
- The total balance on Home counts to its new value when it changes; it changes instantly when "Remove animations" is on.
- The total balance card on Home shows the chosen icon shape (Settings → Appearance → Icon shape) as a faint decoration in one of its corners; it turns slowly and responds to a press, and stays still when "Remove animations" is on.

### Changed

- The default account can no longer be archived; make another account the default first.

## [0.1.0-beta.5] - 2026-10-23

### Added

- What's new page, opened from Settings → Version.
- Line view for the spending trend in Analysis, selectable alongside the column view.
- Tooltips on icon-only buttons, shown on long press.

### Changed

- Category and account editors show Save, Archive and Delete in a floating toolbar,
  consistent with transaction details.

## [0.1.0-beta.4] - 2026-10-23

### Added

- Colour palettes for use when dynamic colour is off: Monochrome, Blue, Green, Purple,
  Rose and Amber (Settings → Appearance → Palette). The selection is included in backups.

### Changed

- Navigation transitions follow Material 3 patterns: tabs fade through, pages slide
  forward and back, and transactions and Home account cards expand into their pages.
- Changing the period in Transactions and Analysis slides the content in the direction of
  travel. The period selector stays fixed at the top.
- List rows animate into place when items are added, removed, filtered or archived.
- The date picker expands from its field, and new transaction items expand into place.
- Animations are replaced by fades when the system "Remove animations" setting is on.
- Backups run without blocking the app; progress is shown on the Back up row.
- Backup, restore and wipe errors are shown in a dialog.

## [0.1.0-beta.3] - 2026-10-19

### Added

- Itemised transactions: expenses and income can be split into items with a quantity and
  unit price. The transaction amount is the sum of its items.
- Data backup to a `.fiscus` file, with a choice of transactions, accounts and categories;
  settings; and profile (Settings → Data).
- Restore from a backup file, including backups made by earlier versions.
- Wipe data, which resets the app to its freshly installed state.

### Changed

- The database is upgraded on first launch to support itemised transactions. Existing
  data is preserved.

## [0.1.0-beta.2] - 2026-10-18

### Added

- Profile name and photo (Settings → Profile). Photos are selected with the system photo
  picker and stored on the device.
- Time-of-day greeting with the profile name on Home, with the profile photo in the app
  bar.
- Opening balance for accounts. A negative value records money owed.

### Changed

- New app icon, also used on the splash screen.
- The splash screen background uses the wallpaper colour on Android 12 and later.
- The colour scheme used when dynamic colour is off is now monochrome.
- Rounded icons throughout, and buttons change shape when pressed.
- Edit is the floating action button on transaction details.
- Home account cards are always shown at full size.
- Home shows an empty state when there are no transactions.

### Fixed

- Default categories and the Cash account were missing after Android restored a backup
  during installation.

## [0.1.0-beta.1] - 2026-10-18

### Added

- Income, expense and transfer transactions with an amount, title, category or transfer
  accounts, account, date and optional note.
- Home with total balance, the current month's income and spending, an accounts carousel
  and the ten most recent transactions.
- Transactions list by month, day or custom date range, with type, account and category
  filters, and search across titles and notes.
- Transaction details with edit and delete.
- Analysis with spending, change from the previous period, daily average, savings rate,
  spending by category, a spending trend and the largest expenses.
- Fourteen default categories and a Cash account.
- Category and account management: add, rename, change icon, archive, restore and delete
  unused items.
- Theme, dynamic colour, icon shape, app bar size and scroll behaviour, and bottom bar
  label settings.

[Unreleased]: https://github.com/prajwalpawar7744/fiscus/compare/v0.1.0-beta.5...HEAD
[0.1.0-beta.5]: https://github.com/prajwalpawar7744/fiscus/compare/v0.1.0-beta.4...v0.1.0-beta.5
[0.1.0-beta.4]: https://github.com/prajwalpawar7744/fiscus/compare/v0.1.0-beta.3...v0.1.0-beta.4
[0.1.0-beta.3]: https://github.com/prajwalpawar7744/fiscus/compare/v0.1.0-beta.2...v0.1.0-beta.3
[0.1.0-beta.2]: https://github.com/prajwalpawar7744/fiscus/compare/v0.1.0-beta.1...v0.1.0-beta.2
[0.1.0-beta.1]: https://github.com/prajwalpawar7744/fiscus/releases/tag/v0.1.0-beta.1
