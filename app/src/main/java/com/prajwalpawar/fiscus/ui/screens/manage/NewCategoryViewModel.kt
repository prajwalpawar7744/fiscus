package com.prajwalpawar.fiscus.ui.screens.manage

import android.database.sqlite.SQLiteConstraintException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prajwalpawar.fiscus.data.model.Category
import com.prajwalpawar.fiscus.data.model.CategoryType
import com.prajwalpawar.fiscus.data.repository.CategoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NewCategoryUiState(
    val name: String = "",
    val type: CategoryType = CategoryType.EXPENSE,
    val iconKey: String = "category",
    val isDuplicate: Boolean = false,
    val isSaving: Boolean = false
) {
    val canSave: Boolean get() = name.isNotBlank() && !isSaving
}

class NewCategoryViewModel(
    private val categoryRepository: CategoryRepository,
    initialType: CategoryType
) : ViewModel() {
    private val state = MutableStateFlow(NewCategoryUiState(type = initialType))
    val uiState: StateFlow<NewCategoryUiState> = state.asStateFlow()

    fun setName(name: String) {
        state.update { it.copy(name = name, isDuplicate = false) }
    }

    fun setType(type: CategoryType) {
        // Names are unique per type, so a duplicate may no longer apply.
        state.update { it.copy(type = type, isDuplicate = false) }
    }

    fun setIcon(iconKey: String) {
        state.update { it.copy(iconKey = iconKey) }
    }

    fun save(onSaved: (id: Long, type: CategoryType) -> Unit) {
        val current = state.value
        if (!current.canSave) return

        val name = current.name.trim()

        viewModelScope.launch {
            state.update { it.copy(isSaving = true) }

            // The database index is case-sensitive; treat "food" and "Food" as the same.
            val exists = categoryRepository.observeCategories(current.type).first()
                .any { it.name.equals(name, ignoreCase = true) }

            val id = if (exists) {
                null
            } else {
                try {
                    categoryRepository.addCategory(
                        Category(name = name, type = current.type, iconKey = current.iconKey)
                    )
                } catch (e: SQLiteConstraintException) {
                    // Matches an archived category with the same name.
                    null
                }
            }

            state.update { it.copy(isSaving = false, isDuplicate = id == null) }
            id?.let { onSaved(it, current.type) }
        }
    }
}
