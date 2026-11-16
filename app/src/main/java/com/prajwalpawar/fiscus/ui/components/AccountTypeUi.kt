package com.prajwalpawar.fiscus.ui.components

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.prajwalpawar.fiscus.R
import com.prajwalpawar.fiscus.model.AccountType

/** Material Symbols Rounded icon for an [AccountType] (ADR-010, ADR-033). */
@get:DrawableRes
val AccountType.iconRes: Int
    get() = when (this) {
        AccountType.CASH -> R.drawable.ic_payments_rounded
        AccountType.BANK -> R.drawable.ic_account_balance_rounded
        AccountType.CARD -> R.drawable.ic_credit_card_rounded
        AccountType.WALLET -> R.drawable.ic_account_balance_wallet_rounded
    }

/** Localized label for an [AccountType] (ADR-036). */
@get:StringRes
val AccountType.labelRes: Int
    get() = when (this) {
        AccountType.CASH -> R.string.account_type_cash
        AccountType.BANK -> R.string.account_type_bank
        AccountType.CARD -> R.string.account_type_card
        AccountType.WALLET -> R.string.account_type_wallet
    }

/** The order account types are offered in. */
val AccountTypeDisplayOrder: List<AccountType> =
    listOf(AccountType.CASH, AccountType.BANK, AccountType.CARD, AccountType.WALLET)
