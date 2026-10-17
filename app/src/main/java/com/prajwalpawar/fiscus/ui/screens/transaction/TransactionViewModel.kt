package com.prajwalpawar.fiscus.ui.screens.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prajwalpawar.fiscus.data.model.Account
import com.prajwalpawar.fiscus.data.model.Category
import com.prajwalpawar.fiscus.data.model.CategoryType
import com.prajwalpawar.fiscus.data.model.Transaction
import com.prajwalpawar.fiscus.data.model.TransactionType
import com.prajwalpawar.fiscus.data.repository.AccountRepository
import com.prajwalpawar.fiscus.data.repository.CategoryRepository
import com.prajwalpawar.fiscus.data.repository.TransactionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.util.Calendar
import java.util.TimeZone

// What the user has entered so far; lives in the ViewModel so it survives rotation
// and closing the sheet by accident.
data class TransactionForm(
    val type: TransactionType = TransactionType.EXPENSE,
    val amount: String = "",
    val categoryId: Long? = null,
    // The account for income/expense, or the "from" account for a transfer.
    val accountId: Long? = null,
    val toAccountId: Long? = null,
    val dateMillis: Long = todayAsUtcMillis(),
    val note: String = ""
)

// The first thing stopping the form from being submitted, in field order.
enum class TransactionFormIssue {
    AMOUNT,
    CATEGORY,
    ACCOUNT,
    NEEDS_TWO_ACCOUNTS,
    TO_ACCOUNT
}

data class TransactionUiState (
    val accounts: List<Account> = emptyList(),
    val categories: List<Category> = emptyList(),
    val form: TransactionForm = TransactionForm(),
    // The chosen account, or the default one when the user hasn't picked yet.
    val selectedAccountId: Long? = null,
    // Transfer targets: every account except the "from" one.
    val toAccounts: List<Account> = emptyList(),
    val selectedToAccountId: Long? = null,
    val issue: TransactionFormIssue? = TransactionFormIssue.AMOUNT,
    val isSaving: Boolean = false,
    val saveFailed: Boolean = false
) {
    val canSubmit: Boolean get() = issue == null && !isSaving
}

class TransactionViewModel (
    accountRepository: AccountRepository,
    categoryRepository: CategoryRepository,
    private val transactionRepository: TransactionRepository
): ViewModel() {
    private val form = MutableStateFlow(TransactionForm())
    private val isSaving = MutableStateFlow(false)
    private val saveFailed = MutableStateFlow(false)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val categories = form
        .map { it.type }
        .distinctUntilChanged()
        .flatMapLatest { type ->
            when (type) {
                TransactionType.EXPENSE ->
                    categoryRepository.observeCategories(CategoryType.EXPENSE)

                TransactionType.INCOME ->
                    categoryRepository.observeCategories(CategoryType.INCOME)

                TransactionType.TRANSFER ->
                    flowOf(emptyList())
            }
        }

    val uiState: StateFlow<TransactionUiState> =
        combine(
            accountRepository.accounts,
            categories,
            form,
            isSaving,
            saveFailed
        ) { accounts, categories, form, isSaving, saveFailed ->
            val selectedAccountId = form.accountId
                ?.takeIf { id -> accounts.any { it.id == id } }
                ?: accounts.firstOrNull { it.isDefault }?.id
                ?: accounts.firstOrNull()?.id

            val toAccounts = accounts.filter { it.id != selectedAccountId }
            val selectedToAccountId = form.toAccountId
                ?.takeIf { id -> toAccounts.any { it.id == id } }

            TransactionUiState(
                accounts = accounts,
                categories = categories,
                form = form,
                selectedAccountId = selectedAccountId,
                toAccounts = toAccounts,
                selectedToAccountId = selectedToAccountId,
                issue = findIssue(
                    form = form,
                    categories = categories,
                    accountCount = accounts.size,
                    selectedAccountId = selectedAccountId,
                    selectedToAccountId = selectedToAccountId
                ),
                isSaving = isSaving,
                saveFailed = saveFailed
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = TransactionUiState()
        )

    fun setTransactionType(type: TransactionType) {
        form.update { current ->
            if (current.type == type) {
                current
            } else {
                // Categories belong to one type, so a switch invalidates the choice.
                current.copy(type = type, categoryId = null)
            }
        }
    }

    fun setAmount(value: String) {
        if (isAmountInput(value)) {
            form.update { it.copy(amount = value) }
        }
    }

    fun selectCategory(id: Long) {
        form.update { it.copy(categoryId = id) }
    }

    fun selectAccount(id: Long) {
        form.update { it.copy(accountId = id) }
    }

    fun selectToAccount(id: Long) {
        form.update { it.copy(toAccountId = id) }
    }

    fun setDate(millis: Long) {
        form.update { it.copy(dateMillis = millis) }
    }

    fun setNote(value: String) {
        form.update { it.copy(note = value) }
    }

    fun saveTransaction(onSaved: () -> Unit) {
        val state = uiState.value
        if (!state.canSubmit) return

        val transaction = state.toTransaction() ?: return

        viewModelScope.launch {
            isSaving.value = true
            saveFailed.value = false

            val result = runCatching {
                transactionRepository.addTransaction(transaction)
            }

            isSaving.value = false

            if (result.isSuccess) {
                // Next sheet starts empty; a failed save keeps the draft to retry.
                form.value = TransactionForm()
                onSaved()
            } else {
                saveFailed.value = true
            }
        }
    }
}

private fun TransactionUiState.toTransaction(): Transaction? {
    val amount = amountToMinorUnits(form.amount) ?: return null
    val note = form.note.trim().ifEmpty { null }

    return when (form.type) {
        TransactionType.EXPENSE,
        TransactionType.INCOME -> Transaction(
            type = form.type,
            amount = amount,
            categoryId = form.categoryId,
            accountId = selectedAccountId,
            note = note,
            date = form.dateMillis
        )

        TransactionType.TRANSFER -> Transaction(
            type = form.type,
            amount = amount,
            sourceAccountId = selectedAccountId,
            destinationAccountId = selectedToAccountId,
            note = note,
            date = form.dateMillis
        )
    }
}

internal fun findIssue(
    form: TransactionForm,
    categories: List<Category>,
    accountCount: Int,
    selectedAccountId: Long?,
    selectedToAccountId: Long?
): TransactionFormIssue? {
    val amount = amountToMinorUnits(form.amount)

    return when {
        amount == null || amount <= 0L ->
            TransactionFormIssue.AMOUNT

        form.type == TransactionType.TRANSFER -> when {
            accountCount < 2 -> TransactionFormIssue.NEEDS_TWO_ACCOUNTS
            selectedAccountId == null -> TransactionFormIssue.ACCOUNT
            selectedToAccountId == null -> TransactionFormIssue.TO_ACCOUNT
            else -> null
        }

        categories.none { it.id == form.categoryId } ->
            TransactionFormIssue.CATEGORY

        selectedAccountId == null ->
            TransactionFormIssue.ACCOUNT

        else -> null
    }
}

// Digits (at most 12 before the point, so the stored Long can't overflow)
// with at most one decimal point and two decimal places.
internal fun isAmountInput(value: String): Boolean {
    return value.matches(Regex("""\d{0,12}(\.\d{0,2})?"""))
}

// "12.5" -> 1250. Null when the text isn't a number yet (e.g. "" or ".").
internal fun amountToMinorUnits(value: String): Long? {
    return value.toBigDecimalOrNull()
        ?.movePointRight(2)
        ?.let { runCatching { it.longValueExact() }.getOrNull() }
}

private fun todayAsUtcMillis(): Long {
    val today = Calendar.getInstance()

    // DatePicker works in UTC midnights, so store the local date that way.
    return Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        clear()
        set(
            today.get(Calendar.YEAR),
            today.get(Calendar.MONTH),
            today.get(Calendar.DAY_OF_MONTH)
        )
    }.timeInMillis
}
