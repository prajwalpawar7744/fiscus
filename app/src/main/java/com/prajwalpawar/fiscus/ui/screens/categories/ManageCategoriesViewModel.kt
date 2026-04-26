package com.prajwalpawar.fiscus.ui.screens.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prajwalpawar.fiscus.data.local.pref.PreferenceManager
import com.prajwalpawar.fiscus.domain.model.Category
import com.prajwalpawar.fiscus.domain.model.TransactionType
import com.prajwalpawar.fiscus.domain.repository.FiscusRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ManageCategoriesUiState(
    val categories: List<Category> = emptyList(),
    val topBarStyle: String = "standard",
    val areAnimationsEnabled: Boolean = true
)

@HiltViewModel
class ManageCategoriesViewModel @Inject constructor(
    private val repository: FiscusRepository,
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    val uiState: StateFlow<ManageCategoriesUiState> = combine(
        repository.getCategories(),
        preferenceManager.topBarStyle,
        preferenceManager.areAnimationsEnabled
    ) { categories, topBarStyle, areAnimationsEnabled ->
        ManageCategoriesUiState(
            categories = categories,
            topBarStyle = topBarStyle,
            areAnimationsEnabled = areAnimationsEnabled
        )
    }.stateIn(
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
