package com.prajwalpawar.fiscus.ui.screens.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prajwalpawar.fiscus.data.model.TransactionDetails
import com.prajwalpawar.fiscus.data.model.TransactionItem
import com.prajwalpawar.fiscus.data.repository.TransactionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TransactionDetailsUiState(
    val isLoading: Boolean = true,
    // Null after loading means the transaction no longer exists (e.g. just deleted).
    val details: TransactionDetails? = null,
    // The itemised breakdown; empty for a plain transaction.
    val items: List<TransactionItem> = emptyList()
)

class TransactionDetailsViewModel(
    private val transactionRepository: TransactionRepository,
    private val transactionId: Long
) : ViewModel() {
    // Live, so edits made in the sheet show up here straight away.
    val uiState: StateFlow<TransactionDetailsUiState> =
        combine(
            transactionRepository.observeTransactionDetails(transactionId),
            transactionRepository.observeItems(transactionId)
        ) { details, items ->
            TransactionDetailsUiState(isLoading = false, details = details, items = items)
        }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = TransactionDetailsUiState()
            )

    fun delete(onDeleted: () -> Unit) {
        viewModelScope.launch {
            transactionRepository.deleteTransaction(transactionId)
            onDeleted()
        }
    }
}
