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

data class CategoryEditorUiState(
    val name: String = "",
    val type: CategoryType = CategoryType.EXPENSE,
    val iconKey: String = "category",
    val isDuplicate: Boolean = false,
    val isSaving: Boolean = false,
    // False only while an existing category is being loaded.
    val isLoaded: Boolean = true,
    // Null when creating a new category.
    val manage: ItemManageState? = null
) {
    val isEditing: Boolean get() = manage != null
    val canSave: Boolean get() = isLoaded && name.isNotBlank() && !isSaving

    // Existing transactions would end up under a category of the wrong type.
    val isTypeLocked: Boolean get() = (manage?.usageCount ?: 0) > 0
}

// Creates a category, or edits one when [editingId] is set.
class CategoryEditorViewModel(
    private val categoryRepository: CategoryRepository,
    initialType: CategoryType,
    private val editingId: Long?
) : ViewModel() {
    private val state = MutableStateFlow(
        CategoryEditorUiState(type = initialType, isLoaded = editingId == null)
    )
    val uiState: StateFlow<CategoryEditorUiState> = state.asStateFlow()

    private var original: Category? = null

    init {
        if (editingId != null) {
            viewModelScope.launch { load(editingId) }
        }
    }

    private suspend fun load(id: Long) {
        val category = categoryRepository.getCategory(id) ?: return
        original = category

        state.value = CategoryEditorUiState(
            name = category.name,
            type = category.type,
            iconKey = category.iconKey,
            isLoaded = true,
            manage = ItemManageState(
                isArchived = category.isArchived,
                isDefault = category.isDefault,
                usageCount = categoryRepository.usageCount(id),
                canArchive = categoryRepository.activeCount(category.type) > 1
            )
        )
    }

    fun setName(name: String) {
        state.update { it.copy(name = name, isDuplicate = false) }
    }

    fun setType(type: CategoryType) {
        if (state.value.isTypeLocked) return
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
                .any { it.id != editingId && it.name.equals(name, ignoreCase = true) }

            val id = if (exists) {
                null
            } else {
                try {
                    val existing = original
                    if (existing != null) {
                        categoryRepository.updateCategory(
                            existing.copy(
                                name = name,
                                type = current.type,
                                iconKey = current.iconKey
                            )
                        )
                        existing.id
                    } else {
                        categoryRepository.addCategory(
                            Category(name = name, type = current.type, iconKey = current.iconKey)
                        )
                    }
                } catch (e: SQLiteConstraintException) {
                    // Matches an archived category with the same name.
                    null
                }
            }

            state.update { it.copy(isSaving = false, isDuplicate = id == null) }
            id?.let { onSaved(it, current.type) }
        }
    }

    fun toggleArchived(onDone: () -> Unit) {
        val id = editingId ?: return
        val manage = state.value.manage ?: return

        viewModelScope.launch {
            categoryRepository.setArchived(id, !manage.isArchived)
            onDone()
        }
    }

    fun delete(onDone: () -> Unit) {
        val id = editingId ?: return

        viewModelScope.launch {
            categoryRepository.deleteCategory(id)
            onDone()
        }
    }
}
