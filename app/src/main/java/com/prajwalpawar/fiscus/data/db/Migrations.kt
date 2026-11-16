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

/**
 * v2 → v3: adds `accounts.isDefault` (ADR-037). The oldest active account becomes the default,
 * so existing users keep today's behaviour (the first account preselected).
 */
object Migration2To3 : Migration(2, 3) {
    override suspend fun migrate(connection: SQLiteConnection) {
        connection.execSQL("ALTER TABLE accounts ADD COLUMN isDefault INTEGER NOT NULL DEFAULT 0")
        connection.execSQL(
            "UPDATE accounts SET isDefault = 1 " +
                "WHERE id = (SELECT MIN(id) FROM accounts WHERE archived = 0)",
        )
    }
}

/**
 * v3 → v4: adds `categories.isSeeded` (ADR-041). The starter set was inserted first when the
 * database was created, and categories couldn't be deleted before v4, so the lowest
 * [SEEDED_CATEGORY_COUNT] ids are the seeded ones.
 */
object Migration3To4 : Migration(3, 4) {
    const val SEEDED_CATEGORY_COUNT = 13

    override suspend fun migrate(connection: SQLiteConnection) {
        connection.execSQL("ALTER TABLE categories ADD COLUMN isSeeded INTEGER NOT NULL DEFAULT 0")
        connection.execSQL(
            "UPDATE categories SET isSeeded = 1 WHERE id IN " +
                "(SELECT id FROM categories ORDER BY id LIMIT $SEEDED_CATEGORY_COUNT)",
        )
    }
}
