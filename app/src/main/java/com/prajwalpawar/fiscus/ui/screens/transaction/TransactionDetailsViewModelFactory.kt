package com.prajwalpawar.fiscus.ui.screens.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.prajwalpawar.fiscus.data.repository.TransactionRepository

class TransactionDetailsViewModelFactory(
    private val transactionRepository: TransactionRepository,
    private val transactionId: Long
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        if (modelClass.isAssignableFrom(TransactionDetailsViewModel::class.java)) {
            return TransactionDetailsViewModel(
                transactionRepository = transactionRepository,
                transactionId = transactionId
            ) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class: ${modelClass.name}"
        )
    }
}
