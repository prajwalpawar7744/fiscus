package com.prajwalpawar.fiscus.data.db

import android.content.Context
import androidx.annotation.StringRes
import androidx.room3.RoomDatabase
import androidx.sqlite.SQLiteConnection
import com.prajwalpawar.fiscus.R
import com.prajwalpawar.fiscus.core.money.defaultCurrencyCode
import com.prajwalpawar.fiscus.model.AccountType
import com.prajwalpawar.fiscus.model.CategoryKind

/**
 * Inserts the starter data once, when the database file is created (ADR-016): a "Cash" account
 * in the device's currency and the starter categories. Names are localized at creation time and
 * stored as normal rows, so users can edit them.
 */
class SeedCallback(
    private val context: Context,
    private val currencyCode: String = defaultCurrencyCode(),
) : RoomDatabase.Callback() {

    override suspend fun onCreate(connection: SQLiteConnection) {
        connection.prepare(
            "INSERT INTO accounts (name, type, currencyCode, openingBalanceMinor, archived) " +
                "VALUES (?, ?, ?, 0, 0)",
        ).use { statement ->
            statement.bindText(1, context.getString(R.string.seed_account_cash))
            statement.bindText(2, AccountType.CASH.name)
            statement.bindText(3, currencyCode)
            statement.step()
        }
        connection.prepare(
            "INSERT INTO categories (name, kind, iconKey, archived) VALUES (?, ?, ?, 0)",
        ).use { statement ->
            STARTER_CATEGORIES.forEach { seed ->
                statement.bindText(1, context.getString(seed.name))
                statement.bindText(2, seed.kind.name)
                statement.bindText(3, seed.iconKey)
                statement.step()
                statement.reset()
            }
        }
    }

    private data class SeedCategory(@StringRes val name: Int, val kind: CategoryKind, val iconKey: String)

    private companion object {
        // iconKey = Material Symbols name (ADR-010, ADR-016).
        val STARTER_CATEGORIES = listOf(
            SeedCategory(R.string.seed_category_food, CategoryKind.EXPENSE, "restaurant"),
            SeedCategory(R.string.seed_category_groceries, CategoryKind.EXPENSE, "local_grocery_store"),
            SeedCategory(R.string.seed_category_transport, CategoryKind.EXPENSE, "directions_car"),
            SeedCategory(R.string.seed_category_bills, CategoryKind.EXPENSE, "receipt_long"),
            SeedCategory(R.string.seed_category_shopping, CategoryKind.EXPENSE, "shopping_bag"),
            SeedCategory(R.string.seed_category_health, CategoryKind.EXPENSE, "medical_services"),
            SeedCategory(R.string.seed_category_entertainment, CategoryKind.EXPENSE, "movie"),
            SeedCategory(R.string.seed_category_education, CategoryKind.EXPENSE, "school"),
            SeedCategory(R.string.seed_category_other_expense, CategoryKind.EXPENSE, "category"),
            SeedCategory(R.string.seed_category_salary, CategoryKind.INCOME, "work"),
            SeedCategory(R.string.seed_category_business, CategoryKind.INCOME, "storefront"),
            SeedCategory(R.string.seed_category_gifts, CategoryKind.INCOME, "redeem"),
            SeedCategory(R.string.seed_category_other_income, CategoryKind.INCOME, "payments"),
        )
    }
}
