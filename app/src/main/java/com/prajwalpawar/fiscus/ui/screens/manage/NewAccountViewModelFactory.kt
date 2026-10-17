package com.prajwalpawar.fiscus.ui.screens.manage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.prajwalpawar.fiscus.data.repository.AccountRepository

class NewAccountViewModelFactory (
    private val accountRepository: AccountRepository
): ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        if (modelClass.isAssignableFrom(NewAccountViewModel::class.java)) {
            return NewAccountViewModel(
                accountRepository = accountRepository
            ) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class: ${modelClass.name}"
        )
    }
}
