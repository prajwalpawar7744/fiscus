package com.prajwalpawar.fiscus.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prajwalpawar.fiscus.data.local.pref.PreferenceManager
import com.prajwalpawar.fiscus.domain.model.*
import com.prajwalpawar.fiscus.domain.repository.FiscusRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class DashboardUiState(
    val balance: Double = 0.0,
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val recentTransactions: List<Transaction> = emptyList(),
    val categories: Map<Long, Category> = emptyMap(),
    val accountsMap: Map<Long, Account> = emptyMap(),
    val currency: String = "USD",
    val userName: String = "",
    val userPhotoUri: String? = null,
    val topBarStyle: String = "standard",
    val areAnimationsEnabled: Boolean = true,
    val accounts: List<AccountWithBalance> = emptyList(),
    val selectedTransactionDetail: Transaction? = null,
    val isLoading: Boolean = true,
    val isPrivacyModeEnabled: Boolean = false,
    val isCompactNumberFormatEnabled: Boolean = false
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: FiscusRepository,
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    private val _categories = repository.getCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _userPrefs = combine<Any?, DashboardUserPrefs>(
        preferenceManager.currency,
        preferenceManager.userName,
        preferenceManager.userPhotoUri,
        preferenceManager.topBarStyle,
        preferenceManager.areAnimationsEnabled,
        preferenceManager.isPrivacyModeEnabled,
        preferenceManager.isCompactNumberFormatEnabled
    ) { args ->
        DashboardUserPrefs(
            currency = args[0] as String,
            userName = args[1] as String,
            userPhotoUri = args[2] as? String,
            topBarStyle = args[3] as String,
            areAnimationsEnabled = args[4] as Boolean,
            isPrivacyModeEnabled = args[5] as Boolean,
            isCompactNumberFormatEnabled = args[6] as Boolean
        )
    }

    data class DashboardUserPrefs(
        val currency: String,
        val userName: String,
        val userPhotoUri: String?,
        val topBarStyle: String,
        val areAnimationsEnabled: Boolean,
        val isPrivacyModeEnabled: Boolean,
        val isCompactNumberFormatEnabled: Boolean
    )

    data class DashboardData(
        val income: Double,
        val expense: Double,
        val recent: List<Transaction>,
        val categories: List<Category>,
        val accounts: List<Account>
    )

    private val _dashboardData = combine(
        repository.getTotalAmountByType(TransactionType.INCOME.name),
        repository.getTotalAmountByType(TransactionType.EXPENSE.name),
        repository.getRecentTransactions(5),
        _categories,
        repository.getAccounts()
    ) { income: Double, expense: Double, recent: List<Transaction>, categories: List<Category>, accounts: List<Account> ->
        DashboardData(income, expense, recent, categories, accounts)
    }

    private val _dashboardUiState: StateFlow<DashboardUiState> = combine(
        _dashboardData,
        repository.getTransactions(),
        _userPrefs
    ) { data: DashboardData, allTransactions: List<Transaction>, prefs: DashboardUserPrefs ->
        val accountsWithBalance = data.accounts.map { account ->
            val accountTransactions =
                allTransactions.filter { it.accountId == account.id || it.toAccountId == account.id }
            val accountIncome =
                accountTransactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
            val accountExpense = accountTransactions.filter { it.type == TransactionType.EXPENSE }
                .sumOf { it.amount }
            val transferIn =
                accountTransactions.filter { it.type == TransactionType.TRANSFER && it.toAccountId == account.id }
                    .sumOf { it.amount }
            val transferOut =
                accountTransactions.filter { it.type == TransactionType.TRANSFER && it.accountId == account.id }
                    .sumOf { it.amount }

            AccountWithBalance(
                account = account,
                balance = account.balance + accountIncome - accountExpense + transferIn - transferOut
            )
        }

        DashboardUiState(
            balance = data.income - data.expense + data.accounts.sumOf { it.balance },
            totalIncome = data.income,
            totalExpense = data.expense,
            recentTransactions = data.recent,
            categories = data.categories.associateBy { it.id ?: 0L },
            accountsMap = data.accounts.associateBy { it.id ?: 0L },
            currency = prefs.currency,
            userName = prefs.userName,
            userPhotoUri = prefs.userPhotoUri,
            topBarStyle = prefs.topBarStyle,
            areAnimationsEnabled = prefs.areAnimationsEnabled,
            accounts = accountsWithBalance,
            isLoading = false,
            isPrivacyModeEnabled = prefs.isPrivacyModeEnabled,
            isCompactNumberFormatEnabled = prefs.isCompactNumberFormatEnabled
        )
    }.flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DashboardUiState()
        )

    private val _selectedTransactionDetail = MutableStateFlow<Transaction?>(null)

    val uiState: StateFlow<DashboardUiState> = combine(
        _dashboardUiState,
        _selectedTransactionDetail
    ) { base, selected ->
        base.copy(selectedTransactionDetail = selected)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardUiState())

    fun onTransactionClick(transaction: Transaction) {
        _selectedTransactionDetail.value = transaction
    }

    fun clearSelectedTransaction() {
        _selectedTransactionDetail.value = null
    }
}
