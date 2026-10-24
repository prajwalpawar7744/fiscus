# Fiscus to-do

Suggested order: finish the "before the next release" items for a safe beta.4, then
budgets and recurring transactions.

## Before the next release

- [ ] Run the instrumented tests on the phone (`connectedDebugAndroidTest`, uninstalls the
      app, so back up first): the 1→2 migration test has never run.
- [ ] Test backup, restore and wipe by hand on the phone.
- [ ] Explicit Auto Backup rules: include the database, settings and profile photo; leave out
      caches (the extraction rules are still the empty sample).
- [ ] Home's month: switch to the new month at midnight on the 1st instead of waiting for a
      reopen.
- [ ] Keep the add-transaction draft when Android closes the app in the background.

## Features

- [ ] Budgets: a monthly limit per category, shown as a wavy meter in Analysis and on Home.
- [ ] Recurring transactions: rent, salary, subscriptions added on schedule or as one-tap
      reminders.
- [ ] Export to CSV, for spreadsheets or tax returns (alongside the `.fiscus` backup).
- [ ] Automatic backups: a weekly backup to a folder the user picks, same file format.
- [ ] Quick add: a home-screen widget or Quick Settings tile that opens the add sheet.
- [ ] Home and lock-screen widgets: this month's spending, or total balance.
- [ ] Search inside items (find "milk" in a "Groceries" transaction).
- [ ] Auto-capture UPI payments from notifications: a `NotificationListenerService`
      (user grants notification access once, picks which apps — PhonePe, GPay, bank apps)
      reads "Paid ₹250 to X" notifications and offers them as suggested transactions to
      confirm or edit, never saved silently. Offline. Parsing differs per app and changes
      over time, so keep the patterns easy to update.
- [ ] "Share to Fiscus": register a share target so a PhonePe/GPay receipt shared to Fiscus
      opens the add sheet pre-filled (amount, payee as title, date). No special
      permissions; the fallback when a notification was missed.

## Home hero

- [x] Tap the hero to open the Accounts list (also the accessible way to see every account
      without swiping the carousel). `Card(onClick)`.
- [ ] "↑ ₹X this month" / "↓ ₹X this month" under the total: this month's income minus
      spending. Direction by arrow and words, not colour (text keeps the card's on-colour).
- [ ] Hide-amounts eye: a toggle on the hero that masks every amount on Home as "₹ ••••".
      Saved as a setting and included in settings backups; TalkBack reads "hidden".
- [x] Decorative shape: the user's icon shape, large and faint (~8% on-colour), cropped in
      the hero's top-end corner. Follows the icon-shape setting.
- [ ] 30-day balance sparkline under the amount: total balance at the end of each day, worked
      back from today's total with each day's net change (active accounts only; needs a
      daily-net query, no schema change). Thin line, no axes; TalkBack reads start and end.
- [x] Count-up: the total rolls to its new value when it changes (not on first show); jumps
      instantly with "Remove animations" on.

## Polish

- [ ] Move all text into `strings.xml` (first step toward translations).
- [ ] App lock: fingerprint or PIN on open.
- [ ] Currency choice in Settings instead of always the phone's currency.
- [ ] Tablet and foldable layouts: a navigation rail and two panes on wide screens.
