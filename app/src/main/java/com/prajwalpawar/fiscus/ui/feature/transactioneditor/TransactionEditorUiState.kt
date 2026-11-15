package com.prajwalpawar.fiscus.ui.feature.transactioneditor

import androidx.annotation.StringRes
import com.prajwalpawar.fiscus.core.money.parseMoneyInput
import com.prajwalpawar.fiscus.model.Account
import com.prajwalpawar.fiscus.model.Category
import com.prajwalpawar.fiscus.model.TransactionType
import kotlinx.datetime.LocalDate

/** Immutable UI state for the transaction editor (add or edit). */
data class TransactionEditorUiState(
    val isLoading: Boolean = true,
    val isNew: Boolean = true,
    val type: TransactionType = TransactionType.EXPENSE,
    /** Raw text of the amount field, e.g. "19.9". Parsed into Money only on save. */
    val amountText: String = "",
    @StringRes val amountError: Int? = null,
    /** Active accounts the user can pick from. */
    val accounts: List<Account> = emptyList(),
    val accountId: Long? = null,
    val toAccountId: Long? = null,
    /** Active categories matching [type]; empty for transfers. */
    val categories: List<Category> = emptyList(),
    val categoryId: Long? = null,
    val date: LocalDate? = null,
    val note: String = "",
    val isSaving: Boolean = false,
    /** True once the transaction was saved (or no longer exists); the screen then closes. */
    val isFinished: Boolean = false,
    /** Confirmation to show after a successful save ("Expense added"); null if nothing was saved. */
    @StringRes val resultMessage: Int? = null,
) {
    val selectedAccount: Account? get() = accounts.find { it.id == accountId }

    /** Transfer destinations: other accounts with the same currency as the source. */
    val toAccounts: List<Account>
        get() = selectedAccount?.let { from ->
            accounts.filter { it.id != from.id && it.currencyCode == from.currencyCode }
        }.orEmpty()

    val canSave: Boolean
        get() {
            val account = selectedAccount ?: return false
            if (isLoading || isSaving || date == null) return false
            if (parseMoneyInput(amountText, account.currencyCode) == null) return false
            return when (type) {
                TransactionType.TRANSFER -> toAccounts.any { it.id == toAccountId }
                TransactionType.INCOME, TransactionType.EXPENSE -> categoryId != null
            }
        }
}
