package com.prajwalpawar.fiscus.ui.feature.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prajwalpawar.fiscus.data.repository.CategoryRepository
import com.prajwalpawar.fiscus.model.CategoryKind
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Screen-level state holder for the Categories page (ADR-040): every category of the chosen kind,
 * active then archived, sorted by name, each with its transaction count (ADR-041).
 */
@HiltViewModel
class CategoriesViewModel @Inject constructor(
    categoryRepository: CategoryRepository,
) : ViewModel() {

    private val kind = MutableStateFlow(CategoryKind.EXPENSE)

    val uiState: StateFlow<CategoriesUiState> = combine(
        categoryRepository.observeCategoriesWithUsage(),
        kind,
    ) { categories, kind ->
        val (archived, active) = categories
            .filter { it.category.kind == kind }
            .sortedBy { it.category.name.lowercase() }
            .partition { it.category.archived }
        CategoriesUiState(isLoading = false, kind = kind, active = active, archived = archived)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CategoriesUiState(),
    )

    fun onEvent(event: CategoriesEvent) {
        when (event) {
            is CategoriesEvent.KindSelected -> kind.value = event.kind
        }
    }
}
