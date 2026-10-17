package com.prajwalpawar.fiscus.ui.screens.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prajwalpawar.fiscus.data.model.TransactionDetails
import com.prajwalpawar.fiscus.data.repository.TransactionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

// How many matches search shows; enough for a phone screen of scrolling.
private const val SEARCH_LIMIT = 100

// Waits for a pause in typing before querying.
private const val SEARCH_DEBOUNCE_MS = 200L

// Search across every month, by title or note.
class TransactionSearchViewModel(
    transactionRepository: TransactionRepository
) : ViewModel() {
    private val query = MutableStateFlow("")

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val results: StateFlow<List<TransactionDetails>> = query
        .debounce(SEARCH_DEBOUNCE_MS)
        .flatMapLatest { text ->
            if (text.isBlank()) {
                flowOf(emptyList())
            } else {
                transactionRepository.searchTransactions(text.trim(), SEARCH_LIMIT)
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun setQuery(text: String) {
        query.value = text
    }
}
