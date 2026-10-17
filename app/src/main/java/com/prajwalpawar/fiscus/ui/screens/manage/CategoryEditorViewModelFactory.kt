package com.prajwalpawar.fiscus.ui.screens.manage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.prajwalpawar.fiscus.data.model.CategoryType
import com.prajwalpawar.fiscus.data.repository.CategoryRepository

class CategoryEditorViewModelFactory(
    private val categoryRepository: CategoryRepository,
    private val initialType: CategoryType,
    private val editingId: Long? = null
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        if (modelClass.isAssignableFrom(CategoryEditorViewModel::class.java)) {
            return CategoryEditorViewModel(
                categoryRepository = categoryRepository,
                initialType = initialType,
                editingId = editingId
            ) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class: ${modelClass.name}"
        )
    }
}
