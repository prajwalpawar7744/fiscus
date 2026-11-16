package com.prajwalpawar.fiscus.ui.feature.categoryeditor

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prajwalpawar.fiscus.R
import com.prajwalpawar.fiscus.data.repository.CategoryRepository
import com.prajwalpawar.fiscus.data.repository.TransactionRepository
import com.prajwalpawar.fiscus.model.Category
import com.prajwalpawar.fiscus.model.CategoryKind
import com.prajwalpawar.fiscus.model.TransactionFilter
import com.prajwalpawar.fiscus.ui.components.CategoryIcons
import com.prajwalpawar.fiscus.ui.components.canonicalCategoryIconKey
import com.prajwalpawar.fiscus.ui.navigation.CategoryEditorNavKey
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Screen-level state holder for adding or editing a category (ADR-040). What the user typed lives
 * in [form]; the edited category and its usage come from repository streams; [uiState] combines
 * them with `stateIn` (ARCHITECTURE.md "Forms"). Also archives/unarchives a used category and
 * deletes an unused one. Receives its nav key through assisted injection (ADR-018).
 */
@HiltViewModel(assistedFactory = CategoryEditorViewModel.Factory::class)
class CategoryEditorViewModel @AssistedInject constructor(
    @Assisted private val navKey: CategoryEditorNavKey,
    private val categoryRepository: CategoryRepository,
    transactionRepository: TransactionRepository,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(navKey: CategoryEditorNavKey): CategoryEditorViewModel
    }

    private val form = MutableStateFlow(CategoryForm(kind = navKey.kind))

    private val original: Flow<Category?> =
        navKey.categoryId?.let(categoryRepository::observeCategory) ?: flowOf(null)

    private val transactionCount: Flow<Int> = navKey.categoryId?.let { id ->
        transactionRepository.observeTransactions(TransactionFilter(categoryIds = setOf(id)))
            .map { it.size }
    } ?: flowOf(0)

    val uiState: StateFlow<CategoryEditorUiState> =
        combine(form, original, transactionCount) { form, original, count ->
            form.toUiState(original, count)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = CategoryEditorUiState(kind = navKey.kind),
        )

    init {
        viewModelScope.launch { loadInitialForm() }
    }

    fun onEvent(event: CategoryEditorEvent) {
        when (event) {
            is CategoryEditorEvent.NameChanged -> form.update { it.copy(name = event.name) }
            is CategoryEditorEvent.KindChanged ->
                if (!uiState.value.isKindLocked) form.update { it.copy(kind = event.kind) }
            is CategoryEditorEvent.IconChanged -> form.update { it.copy(iconKey = event.iconKey) }
            CategoryEditorEvent.Save -> save()
            CategoryEditorEvent.ArchiveToggled -> toggleArchived()
            CategoryEditorEvent.DeleteRequested ->
                form.update { it.copy(showDeleteConfirmation = uiState.value.canDelete) }
            CategoryEditorEvent.DeleteDismissed ->
                form.update { it.copy(showDeleteConfirmation = false) }
            CategoryEditorEvent.DeleteConfirmed -> delete()
            CategoryEditorEvent.UserMessageShown -> form.update { it.copy(userMessage = null) }
        }
    }

    /** Fills the form from the edited category; finishes when it's gone. */
    private suspend fun loadInitialForm() {
        if (navKey.categoryId == null) {
            form.update { it.copy(isLoaded = true, iconKey = CategoryIcons.first().key) }
            return
        }
        val existing = original.first()
        if (existing == null) {
            form.update { it.copy(isLoaded = true, isFinished = true) }
            return
        }
        form.update {
            it.copy(
                isLoaded = true,
                isNew = false,
                name = existing.name,
                kind = existing.kind,
                iconKey = canonicalCategoryIconKey(existing.iconKey),
            )
        }
    }

    private fun save() {
        // The latest form, not uiState.value, which can lag the last keystroke (LESSONS #12).
        val current = form.value
        viewModelScope.launch {
            val existing = original.first()
            val count = transactionCount.first()
            val state = current.toUiState(existing, count)
            if (!state.canSave) return@launch
            form.update { it.copy(isSaving = true) }
            categoryRepository.save(
                Category(
                    id = existing?.id ?: 0L,
                    name = current.name.trim(),
                    // Re-checked: a used category keeps its kind even if the form changed first.
                    kind = if (count > 0 && existing != null) existing.kind else current.kind,
                    iconKey = current.iconKey,
                    archived = existing?.archived ?: false,
                    isSeeded = existing?.isSeeded ?: false,
                ),
            )
            val message = if (existing == null) {
                R.string.category_result_added
            } else {
                R.string.category_result_updated
            }
            form.update { it.copy(isSaving = false, isFinished = true, resultMessage = message) }
        }
    }

    private fun toggleArchived() {
        val id = navKey.categoryId ?: return
        viewModelScope.launch {
            val existing = original.first() ?: return@launch
            categoryRepository.setArchived(id, !existing.archived)
            val message = if (existing.archived) {
                R.string.category_result_unarchived
            } else {
                R.string.category_result_archived
            }
            form.update { it.copy(userMessage = message) }
        }
    }

    private fun delete() {
        form.update { it.copy(showDeleteConfirmation = false) }
        val id = navKey.categoryId ?: return
        viewModelScope.launch {
            // Re-checked here: a transaction may have been added since the dialog opened.
            if (transactionCount.first() > 0) return@launch
            // Mark first, so the missing category that follows reads as "deleted".
            form.update { it.copy(deleted = true) }
            categoryRepository.delete(id)
        }
    }
}

/** What the user has entered or done here. The edited category itself is not stored here. */
private data class CategoryForm(
    val isLoaded: Boolean = false,
    val isNew: Boolean = true,
    val name: String = "",
    val kind: CategoryKind,
    val iconKey: String = "",
    val showDeleteConfirmation: Boolean = false,
    val deleted: Boolean = false,
    @StringRes val userMessage: Int? = null,
    val isSaving: Boolean = false,
    val isFinished: Boolean = false,
    @StringRes val resultMessage: Int? = null,
)

private fun CategoryForm.toUiState(original: Category?, transactionCount: Int): CategoryEditorUiState {
    // The edited category disappeared after loading: deleted here, or elsewhere.
    val gone = isLoaded && !isNew && original == null
    return CategoryEditorUiState(
        isLoading = !isLoaded,
        isNew = isNew,
        name = name,
        // A used category shows its stored kind, whatever the form holds.
        kind = if (transactionCount > 0 && original != null) original.kind else kind,
        iconKey = iconKey,
        transactionCount = transactionCount,
        archived = original?.archived ?: false,
        showDeleteConfirmation = showDeleteConfirmation,
        userMessage = userMessage,
        isSaving = isSaving,
        isFinished = isFinished || gone,
        resultMessage = when {
            gone && deleted -> R.string.category_result_deleted
            gone -> null
            else -> resultMessage
        },
    )
}
