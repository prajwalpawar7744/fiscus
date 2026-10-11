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
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

data class TransactionUiState (
    val accounts: List<Account> = emptyList(),
    val categories: List<Category> = emptyList(),
    val transactionType: TransactionType = TransactionType.EXPENSE
)

class TransactionViewModel (
    accountRepository: AccountRepository,
    categoryRepository: CategoryRepository
): ViewModel() {
    private val transactionType =
        MutableStateFlow(TransactionType.EXPENSE)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val categories: StateFlow<List<Category>> = transactionType.flatMapLatest { type ->
        when (type) {
            TransactionType.EXPENSE ->
                categoryRepository.observeCategories(
                    CategoryType.EXPENSE
                )

            TransactionType.INCOME ->
                categoryRepository.observeCategories(
                    CategoryType.INCOME
                )

            TransactionType.TRANSFER ->
                flowOf(emptyList())
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    val uiState: StateFlow<TransactionUiState> =
        combine(
            accountRepository.accounts,
            categories,
            transactionType
        ) { accounts, categories, type ->
            TransactionUiState(
                accounts = accounts,
                categories = categories,
                transactionType = type
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = TransactionUiState()
        )

    fun setTransactionType(type: TransactionType) {
        transactionType.value = type
    }
}