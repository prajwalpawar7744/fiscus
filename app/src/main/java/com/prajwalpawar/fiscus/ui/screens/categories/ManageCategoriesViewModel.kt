package com.prajwalpawar.fiscus.ui.screens.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prajwalpawar.fiscus.domain.model.Category
import com.prajwalpawar.fiscus.domain.model.TransactionType
import com.prajwalpawar.fiscus.domain.repository.FiscusRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ManageCategoriesUiState(
    val categories: List<Category> = emptyList()
)

@HiltViewModel
class ManageCategoriesViewModel @Inject constructor(
    private val repository: FiscusRepository
) : ViewModel() {

    val uiState: StateFlow<ManageCategoriesUiState> = repository.getCategories()
        .map { ManageCategoriesUiState(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ManageCategoriesUiState()
        )

    fun addCategory(name: String, icon: String, color: Int, type: TransactionType?) {
        viewModelScope.launch {
            repository.insertCategory(
                Category(
                    name = name,
                    icon = icon,
                    color = color,
                    type = type
                )
            )
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            repository.deleteCategory(category)
        }
    }
}
