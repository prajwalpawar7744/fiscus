package com.prajwalpawar.fiscus.ui.feature.categorydetail

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prajwalpawar.fiscus.R
import com.prajwalpawar.fiscus.data.repository.CategoryRepository
import com.prajwalpawar.fiscus.ui.navigation.CategoryDetailNavKey
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Screen-level state holder for one category (ADR-042), built like the account page's
 * (ADR-036): the category and its transaction count from a repository stream via `stateIn`, plus
 * archive/unarchive and delete (only when unused). Receives its nav key through assisted
 * injection (ADR-018).
 */
@HiltViewModel(assistedFactory = CategoryDetailViewModel.Factory::class)
class CategoryDetailViewModel @AssistedInject constructor(
    @Assisted private val navKey: CategoryDetailNavKey,
    private val categoryRepository: CategoryRepository,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(navKey: CategoryDetailNavKey): CategoryDetailViewModel
    }

    private val actions = MutableStateFlow(Actions())

    val uiState: StateFlow<CategoryDetailUiState> = combine(
        categoryRepository.observeCategoryWithUsage(navKey.categoryId),
        actions,
    ) { category, actions ->
        if (category == null) {
            CategoryDetailUiState(
                categoryId = navKey.categoryId,
                isLoading = false,
                isFinished = true,
                resultMessage = if (actions.deleted) R.string.category_result_deleted else null,
            )
        } else {
            CategoryDetailUiState(
                categoryId = navKey.categoryId,
                isLoading = false,
                category = category,
                showDeleteConfirmation = actions.showDeleteConfirmation,
                userMessage = actions.userMessage,
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CategoryDetailUiState(categoryId = navKey.categoryId),
    )

    fun onEvent(event: CategoryDetailEvent) {
        when (event) {
            CategoryDetailEvent.ArchiveToggled -> toggleArchived()
            CategoryDetailEvent.DeleteRequested ->
                actions.update { it.copy(showDeleteConfirmation = uiState.value.canDelete) }
            CategoryDetailEvent.DeleteDismissed ->
                actions.update { it.copy(showDeleteConfirmation = false) }
            CategoryDetailEvent.DeleteConfirmed -> delete()
            CategoryDetailEvent.UserMessageShown -> actions.update { it.copy(userMessage = null) }
        }
    }

    private fun toggleArchived() {
        val category = uiState.value.category?.category ?: return
        viewModelScope.launch {
            categoryRepository.setArchived(category.id, !category.archived)
            val message = if (category.archived) {
                R.string.category_result_unarchived
            } else {
                R.string.category_result_archived
            }
            actions.update { it.copy(userMessage = message) }
        }
    }

    private fun delete() {
        actions.update { it.copy(showDeleteConfirmation = false) }
        // Re-checked here: a transaction may have been added since the dialog opened.
        if (!uiState.value.canDelete) return
        viewModelScope.launch {
            // Mark first, so the missing category that follows reads as "deleted".
            actions.update { it.copy(deleted = true) }
            categoryRepository.delete(navKey.categoryId)
        }
    }

    /** What the user did on this page; everything else comes from the repository. */
    private data class Actions(
        val showDeleteConfirmation: Boolean = false,
        val deleted: Boolean = false,
        @StringRes val userMessage: Int? = null,
    )
}
