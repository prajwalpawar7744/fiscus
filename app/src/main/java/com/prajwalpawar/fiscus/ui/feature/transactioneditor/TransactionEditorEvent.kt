package com.prajwalpawar.fiscus.ui.feature.transactioneditor

import com.prajwalpawar.fiscus.model.TransactionType
import kotlinx.datetime.LocalDate

/** User actions on the transaction editor. */
sealed interface TransactionEditorEvent {
    data class TypeChanged(val type: TransactionType) : TransactionEditorEvent
    data class AmountChanged(val text: String) : TransactionEditorEvent
    data class AccountSelected(val accountId: Long) : TransactionEditorEvent
    data class ToAccountSelected(val accountId: Long) : TransactionEditorEvent
    data class CategorySelected(val categoryId: Long) : TransactionEditorEvent
    data class DateSelected(val date: LocalDate) : TransactionEditorEvent
    data class NoteChanged(val note: String) : TransactionEditorEvent
    data object Save : TransactionEditorEvent
}
