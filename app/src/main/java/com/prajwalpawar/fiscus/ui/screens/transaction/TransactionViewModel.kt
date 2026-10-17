package com.prajwalpawar.fiscus.ui.screens.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prajwalpawar.fiscus.data.model.Account
import com.prajwalpawar.fiscus.data.model.Category
import com.prajwalpawar.fiscus.data.model.CategoryType
import com.prajwalpawar.fiscus.data.model.TransactionType
import com.prajwalpawar.fiscus.data.repository.AccountRepository
import com.prajwalpawar.fiscus.data.repository.CategoryRepository
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
import java.math.BigDecimal
import java.util.Calendar
import java.util.TimeZone

// What the user has entered so far; lives in the ViewModel so it survives rotation
// and closing the sheet by accident.
data class TransactionForm(
    val type: TransactionType = TransactionType.EXPENSE,
    val amount: String = "",
    val categoryId: Long? = null,
    val accountId: Long? = null,
    val dateMillis: Long = todayAsUtcMillis(),
    val note: String = ""
)

// The first thing stopping the form from being submitted, in field order.
enum class TransactionFormIssue {
    AMOUNT,
    CATEGORY,
    ACCOUNT,
    TRANSFER_UNAVAILABLE
}

data class TransactionUiState (
    val accounts: List<Account> = emptyList(),
    val categories: List<Category> = emptyList(),
    val form: TransactionForm = TransactionForm(),
    // The chosen account, or the default one when the user hasn't picked yet.
    val selectedAccountId: Long? = null,
    val issue: TransactionFormIssue? = TransactionFormIssue.AMOUNT
) {
    val isValid: Boolean get() = issue == null
}

class TransactionViewModel (
    accountRepository: AccountRepository,
    categoryRepository: CategoryRepository
): ViewModel() {
    private val form = MutableStateFlow(TransactionForm())

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
            form
        ) { accounts, categories, form ->
            val selectedAccountId = form.accountId
                ?.takeIf { id -> accounts.any { it.id == id } }
                ?: accounts.firstOrNull { it.isDefault }?.id
                ?: accounts.firstOrNull()?.id

            TransactionUiState(
                accounts = accounts,
                categories = categories,
                form = form,
                selectedAccountId = selectedAccountId,
                issue = findIssue(form, categories, selectedAccountId)
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

    fun setDate(millis: Long) {
        form.update { it.copy(dateMillis = millis) }
    }

    fun setNote(value: String) {
        form.update { it.copy(note = value) }
    }

    // Call after a successful save so the next sheet starts empty.
    fun resetForm() {
        form.value = TransactionForm()
    }
}

private fun findIssue(
    form: TransactionForm,
    categories: List<Category>,
    selectedAccountId: Long?
): TransactionFormIssue? {
    val amount = form.amount.toBigDecimalOrNull()

    return when {
        amount == null || amount <= BigDecimal.ZERO ->
            TransactionFormIssue.AMOUNT

        form.type == TransactionType.TRANSFER ->
            TransactionFormIssue.TRANSFER_UNAVAILABLE

        categories.none { it.id == form.categoryId } ->
            TransactionFormIssue.CATEGORY

        selectedAccountId == null ->
            TransactionFormIssue.ACCOUNT

        else -> null
    }
}

// Digits with at most one decimal point and two decimal places.
internal fun isAmountInput(value: String): Boolean {
    return value.matches(Regex("""\d*(\.\d{0,2})?"""))
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
