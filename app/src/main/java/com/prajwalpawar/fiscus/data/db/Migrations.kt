package com.prajwalpawar.fiscus.data.db

import androidx.room3.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import com.prajwalpawar.fiscus.model.TransactionType

/**
 * v1 → v2: adds the required transaction `title` (ADR-021). Existing rows get a title so none is
 * blank, using the first that applies: the category's name, the note's text, or the localized type
 * label ("Expense"). A manual migration, because the fallback labels come from string resources.
 */
class Migration1To2(private val typeLabels: Map<TransactionType, String>) : Migration(1, 2) {

    init {
        require(TransactionType.entries.all { !typeLabels[it].isNullOrBlank() }) {
            "Every transaction type needs a fallback title"
        }
    }

    override suspend fun migrate(connection: SQLiteConnection) {
        connection.execSQL("ALTER TABLE transactions ADD COLUMN title TEXT NOT NULL DEFAULT ''")
        connection.prepare(
            """
            UPDATE transactions SET title = COALESCE(
                (SELECT name FROM categories WHERE categories.id = transactions.categoryId),
                NULLIF(TRIM(note), ''),
                CASE type WHEN 'EXPENSE' THEN ? WHEN 'INCOME' THEN ? ELSE ? END
            )
            """.trimIndent(),
        ).use { statement ->
            statement.bindText(1, typeLabels.getValue(TransactionType.EXPENSE))
            statement.bindText(2, typeLabels.getValue(TransactionType.INCOME))
            statement.bindText(3, typeLabels.getValue(TransactionType.TRANSFER))
            statement.step()
        }
    }
}
