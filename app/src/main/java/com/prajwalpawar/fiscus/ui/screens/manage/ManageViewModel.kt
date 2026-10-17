package com.prajwalpawar.fiscus.ui.screens.manage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prajwalpawar.fiscus.data.model.AccountBalance
import com.prajwalpawar.fiscus.data.model.CategoryWithUsage
import com.prajwalpawar.fiscus.data.repository.AccountRepository
import com.prajwalpawar.fiscus.data.repository.CategoryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

// Backs the Categories and Accounts list pages; null means still loading.
class ManageViewModel(
    categoryRepository: CategoryRepository,
    accountRepository: AccountRepository
) : ViewModel() {
    val categories: StateFlow<List<CategoryWithUsage>?> =
        categoryRepository.categoriesWithUsage.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    val accounts: StateFlow<List<AccountBalance>?> =
        accountRepository.accountBalances.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )
}
