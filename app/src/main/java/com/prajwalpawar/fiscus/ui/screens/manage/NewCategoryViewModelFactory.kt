package com.prajwalpawar.fiscus.ui.screens.manage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.prajwalpawar.fiscus.data.model.CategoryType
import com.prajwalpawar.fiscus.data.repository.CategoryRepository

class NewCategoryViewModelFactory (
    private val categoryRepository: CategoryRepository,
    private val initialType: CategoryType
): ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        if (modelClass.isAssignableFrom(NewCategoryViewModel::class.java)) {
            return NewCategoryViewModel(
                categoryRepository = categoryRepository,
                initialType = initialType
            ) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class: ${modelClass.name}"
        )
    }
}
