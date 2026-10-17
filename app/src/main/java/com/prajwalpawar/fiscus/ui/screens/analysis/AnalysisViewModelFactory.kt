package com.prajwalpawar.fiscus.ui.screens.analysis

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.prajwalpawar.fiscus.data.repository.TransactionRepository

class AnalysisViewModelFactory (
    private val transactionRepository: TransactionRepository
): ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        if (modelClass.isAssignableFrom(AnalysisViewModel::class.java)) {
            return AnalysisViewModel(
                transactionRepository = transactionRepository
            ) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class: ${modelClass.name}"
        )
    }
}
