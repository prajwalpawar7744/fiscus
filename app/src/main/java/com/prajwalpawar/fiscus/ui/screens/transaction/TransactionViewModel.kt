package com.prajwalpawar.fiscus.ui.screens.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prajwalpawar.fiscus.data.model.Account
import com.prajwalpawar.fiscus.data.model.Category
import com.prajwalpawar.fiscus.data.model.CategoryType
import com.prajwalpawar.fiscus.data.model.Transaction
import com.prajwalpawar.fiscus.data.model.TransactionItem
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
    val title: String = "",
    val note: String = "",
    // Itemised breakdown (expense/income only). When not empty, the amount is their sum.
    val items: List<ItemDraft> = emptyList(),
    // Set when the sheet is editing an existing transaction instead of adding one.
    val editingId: Long? = null
) {
    // Items only count where they're allowed; a transfer keeps them in the draft unused.
    val usesItems: Boolean get() = items.isNotEmpty() && type != TransactionType.TRANSFER

    // The amount text the form submits: typed, or the items' sum.
    val effectiveAmount: String
        get() = if (usesItems) {
            items.sumOf { it.total() ?: 0L }.let { if (it == 0L) "" else minorUnitsToAmountInput(it) }
        } else {
            amount
        }
}

// One item row as typed. [key] keeps rows stable in the list while editing.
data class ItemDraft(
    val key: Long,
    val name: String = "",
    val quantity: String = "1",
    val price: String = ""
) {
    // The line total in hundredths, or null while quantity or price isn't a number yet.
    fun total(): Long? {
        val quantity = amountToMinorUnits(quantity)?.takeIf { it > 0L } ?: return null
        val price = amountToMinorUnits(price)?.takeIf { it > 0L } ?: return null
        return TransactionItem(name, quantity, price).total
    }

    val isComplete: Boolean get() = name.isNotBlank() && total() != null
}

// The first thing stopping the form from being submitted, in field order.
enum class TransactionFormIssue {
    ITEMS,
    AMOUNT,
    TITLE,
    CATEGORY,
    ACCOUNT,
    NEEDS_TWO_ACCOUNTS,
    TO_ACCOUNT
}

data class TransactionUiState(
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

class TransactionViewModel(
    accountRepository: AccountRepository,
    categoryRepository: CategoryRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {
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

    private var nextItemKey = 0L

    fun addItem() {
        form.update { it.copy(items = it.items + ItemDraft(key = nextItemKey++)) }
    }

    fun updateItem(key: Long, transform: (ItemDraft) -> ItemDraft) {
        form.update { current ->
            current.copy(items = current.items.map { item ->
                if (item.key != key) {
                    item
                } else {
                    // Quantity and price follow the same rules as the amount field.
                    val updated = transform(item)
                    item.copy(
                        name = updated.name,
                        quantity = updated.quantity.takeIf(::isAmountInput) ?: item.quantity,
                        price = updated.price.takeIf(::isAmountInput) ?: item.price
                    )
                }
            })
        }
    }

    fun removeItem(key: Long) {
        form.update { current -> current.copy(items = current.items.filterNot { it.key == key }) }
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

    fun setTitle(value: String) {
        form.update { it.copy(title = value) }
    }

    fun setNote(value: String) {
        form.update { it.copy(note = value) }
    }

    // An unfinished "add" draft, set aside while an existing transaction is edited.
    private var setAsideDraft: TransactionForm? = null

    // Loads an existing transaction into the form, then calls [onLoaded] to show the sheet.
    fun startEditing(id: Long, onLoaded: () -> Unit) {
        viewModelScope.launch {
            val transaction = transactionRepository.getTransaction(id) ?: return@launch

            if (form.value.editingId == null) {
                setAsideDraft = form.value
            }
            saveFailed.value = false

            val isTransfer = transaction.type == TransactionType.TRANSFER

            form.value = TransactionForm(
                type = transaction.type,
                amount = minorUnitsToAmountInput(transaction.amount),
                categoryId = transaction.categoryId,
                accountId = if (isTransfer) transaction.sourceAccountId else transaction.accountId,
                toAccountId = transaction.destinationAccountId,
                dateMillis = transaction.date,
                title = transaction.title,
                note = transaction.note.orEmpty(),
                items = transaction.items.map { item ->
                    ItemDraft(
                        key = nextItemKey++,
                        name = item.name,
                        quantity = minorUnitsToAmountInput(item.quantity),
                        price = minorUnitsToAmountInput(item.unitPrice)
                    )
                },
                editingId = id
            )
            onLoaded()
        }
    }

    // Leaving edit mode without saving: bring back any set-aside draft.
    fun cancelEditing() {
        if (form.value.editingId != null) {
            finishEditing()
        }
    }

    private fun finishEditing() {
        form.value = setAsideDraft ?: TransactionForm()
        setAsideDraft = null
    }

    fun saveTransaction(onSaved: () -> Unit) {
        val state = uiState.value
        if (!state.canSubmit) return

        val editingId = state.form.editingId
        val transaction = state.toTransaction() ?: return

        viewModelScope.launch {
            isSaving.value = true
            saveFailed.value = false

            val result = runCatching {
                if (editingId != null) {
                    transactionRepository.updateTransaction(transaction.copy(id = editingId))
                } else {
                    transactionRepository.addTransaction(transaction)
                }
            }

            isSaving.value = false

            if (result.isSuccess) {
                // Next sheet starts empty (or back on the set-aside draft);
                // a failed save keeps the form to retry.
                if (editingId != null) finishEditing() else form.value = TransactionForm()
                onSaved()
            } else {
                saveFailed.value = true
            }
        }
    }
}

private fun TransactionUiState.toTransaction(): Transaction? {
    val amount = amountToMinorUnits(form.effectiveAmount) ?: return null
    val items = if (form.usesItems) {
        form.items.map { item ->
            TransactionItem(
                name = item.name.trim(),
                quantity = amountToMinorUnits(item.quantity) ?: return null,
                unitPrice = amountToMinorUnits(item.price) ?: return null
            )
        }
    } else {
        emptyList()
    }
    val title = form.title.trim()
    val note = form.note.trim().ifEmpty { null }

    return when (form.type) {
        TransactionType.EXPENSE,
        TransactionType.INCOME -> Transaction(
            type = form.type,
            amount = amount,
            categoryId = form.categoryId,
            accountId = selectedAccountId,
            title = title,
            note = note,
            date = form.dateMillis,
            items = items
        )

        TransactionType.TRANSFER -> Transaction(
            type = form.type,
            amount = amount,
            sourceAccountId = selectedAccountId,
            destinationAccountId = selectedToAccountId,
            title = title,
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
    val amount = amountToMinorUnits(form.effectiveAmount)

    return when {
        form.usesItems && form.items.any { !it.isComplete } ->
            TransactionFormIssue.ITEMS

        amount == null || amount <= 0L ->
            TransactionFormIssue.AMOUNT

        form.title.isBlank() ->
            TransactionFormIssue.TITLE

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

// 1250 -> "12.5", 1000 -> "10": the way the user would have typed it.
internal fun minorUnitsToAmountInput(hundredths: Long): String {
    return BigDecimal.valueOf(hundredths, 2).stripTrailingZeros().toPlainString()
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
