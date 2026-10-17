package com.prajwalpawar.fiscus.ui.screens.manage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.prajwalpawar.fiscus.data.repository.AccountRepository

class AccountEditorViewModelFactory(
    private val accountRepository: AccountRepository,
    private val editingId: Long? = null
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        if (modelClass.isAssignableFrom(AccountEditorViewModel::class.java)) {
            return AccountEditorViewModel(
                accountRepository = accountRepository,
                editingId = editingId
            ) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class: ${modelClass.name}"
        )
    }
}
