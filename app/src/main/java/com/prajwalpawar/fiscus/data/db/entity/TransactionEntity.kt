package com.prajwalpawar.fiscus.data.db.entity

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey
import com.prajwalpawar.fiscus.model.TransactionType

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.RESTRICT,
        ),
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["toAccountId"],
            onDelete = ForeignKey.RESTRICT,
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [Index("accountId"), Index("toAccountId"), Index("categoryId"), Index("occurredAtMillis")],
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val type: TransactionType,
    /** Required, never blank (schema v2, ADR-021). The default only serves the v1 → v2 migration. */
    @ColumnInfo(defaultValue = "")
    val title: String,
    /** Always positive, in minor units of [currencyCode]. */
    val amountMinor: Long,
    val currencyCode: String,
    val accountId: Long,
    val toAccountId: Long?,
    val categoryId: Long?,
    val note: String,
    val occurredAtMillis: Long,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
)
