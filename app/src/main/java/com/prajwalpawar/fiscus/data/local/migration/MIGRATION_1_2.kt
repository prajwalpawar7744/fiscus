package com.prajwalpawar.fiscus.data.local.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {

    override fun migrate(db: SupportSQLiteDatabase) {

        // Create the categories table.
        db.execSQL(
            """
            CREATE TABLE categories (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL,
                type TEXT NOT NULL,
                iconKey TEXT NOT NULL,
                isDefault INTEGER NOT NULL,
                isArchived INTEGER NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE UNIQUE INDEX index_categories_name_type
            ON categories(name, type)
            """.trimIndent()
        )

        // Create the accounts table.
        db.execSQL(
            """
            CREATE TABLE accounts (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL,
                type TEXT NOT NULL,
                openingBalance INTEGER NOT NULL,
                isDefault INTEGER NOT NULL,
                isArchived INTEGER NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE UNIQUE INDEX index_accounts_name
            ON accounts(name)
            """.trimIndent()
        )

        // Preserve existing category names and their transaction types.
        db.execSQL(
            """
            INSERT OR IGNORE INTO categories (
                name, type, iconKey, isDefault, isArchived
            )
            SELECT DISTINCT
                category,
                type,
                'category',
                0,
                0
            FROM transactions
            WHERE type IN ('EXPENSE', 'INCOME')
            """.trimIndent()
        )

        // Preserve existing account names.
        db.execSQL(
            """
            INSERT OR IGNORE INTO accounts (
                name,
                type,
                openingBalance,
                isDefault,
                isArchived
            )
            SELECT DISTINCT
                account,
                'OTHER',
                0,
                0,
                0
            FROM transactions
            """.trimIndent()
        )

        // Create the new transaction table.
        db.execSQL(
            """
            CREATE TABLE transactions_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                type TEXT NOT NULL,
                amount INTEGER NOT NULL,
                categoryId INTEGER,
                accountId INTEGER,
                sourceAccountId INTEGER,
                destinationAccountId INTEGER,
                note TEXT,
                date INTEGER NOT NULL,

                FOREIGN KEY (categoryId)
                    REFERENCES categories(id)
                    ON UPDATE NO ACTION
                    ON DELETE SET NULL,

                FOREIGN KEY (accountId)
                    REFERENCES accounts(id)
                    ON UPDATE NO ACTION
                    ON DELETE RESTRICT,

                FOREIGN KEY (sourceAccountId)
                    REFERENCES accounts(id)
                    ON UPDATE NO ACTION
                    ON DELETE RESTRICT,

                FOREIGN KEY (destinationAccountId)
                    REFERENCES accounts(id)
                    ON UPDATE NO ACTION
                    ON DELETE RESTRICT
            )
            """.trimIndent()
        )

        // Copy existing transactions and replace names with IDs.
        db.execSQL(
            """
            INSERT INTO transactions_new (
                id,
                type,
                amount,
                categoryId,
                accountId,
                sourceAccountId,
                destinationAccountId,
                note,
                date
            )
            SELECT
                t.id,
                t.type,
                t.amount,

                CASE
                    WHEN t.type IN ('EXPENSE', 'INCOME')
                    THEN c.id
                    ELSE NULL
                END,

                CASE
                    WHEN t.type IN ('EXPENSE', 'INCOME')
                    THEN a.id
                    ELSE NULL
                END,

                CASE
                    WHEN t.type = 'TRANSFER'
                    THEN a.id
                    ELSE NULL
                END,

                NULL,
                t.note,
                t.date

            FROM transactions AS t

            LEFT JOIN categories AS c
                ON c.name = t.category
                AND c.type = t.type

            LEFT JOIN accounts AS a
                ON a.name = t.account
            """.trimIndent()
        )

        db.execSQL("DROP TABLE transactions")
        db.execSQL(
            "ALTER TABLE transactions_new RENAME TO transactions"
        )

        // seed database metadata versioning
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS seed_metadata (
                `key` TEXT NOT NULL,
                version INTEGER NOT NULL,
                PRIMARY KEY(`key`)
            )
            """.trimIndent()
        )

        // Index foreign keys and dates for common queries.
        db.execSQL(
            "CREATE INDEX index_transactions_categoryId " +
                    "ON transactions(categoryId)"
        )
        db.execSQL(
            "CREATE INDEX index_transactions_accountId " +
                    "ON transactions(accountId)"
        )
        db.execSQL(
            "CREATE INDEX index_transactions_sourceAccountId " +
                    "ON transactions(sourceAccountId)"
        )
        db.execSQL(
            "CREATE INDEX index_transactions_destinationAccountId " +
                    "ON transactions(destinationAccountId)"
        )
        db.execSQL(
            "CREATE INDEX index_transactions_date " +
                    "ON transactions(date)"
        )
    }
}