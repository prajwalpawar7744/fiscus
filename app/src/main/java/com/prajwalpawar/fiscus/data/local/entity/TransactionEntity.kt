package com.prajwalpawar.fiscus.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.prajwalpawar.fiscus.data.model.Transaction
import com.prajwalpawar.fiscus.data.model.TransactionType

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["sourceAccountId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["destinationAccountId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index("categoryId"),
        Index("accountId"),
        Index("sourceAccountId"),
        Index("destinationAccountId"),
        Index("date")
    ]
)
data class TransactionEntity (
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val type: String,
    val amount: Long,
    val categoryId: Long?,
    val accountId: Long?,
    val sourceAccountId: Long?,
    val destinationAccountId: Long?,
    val title: String,
    val note: String?,
    val date: Long
)

fun TransactionEntity.toTransaction(): Transaction {
    return Transaction(
        id = id,
        type = TransactionType.valueOf(type),
        amount = amount,
        categoryId = categoryId,
        accountId = accountId,
        sourceAccountId = sourceAccountId,
        destinationAccountId = destinationAccountId,
        title = title,
        note = note,
        date = date
    )
}

fun Transaction.toEntity(): TransactionEntity {
    return TransactionEntity(
        id = id,
        type = type.name,
        amount = amount,
        categoryId = categoryId,
        accountId = accountId,
        sourceAccountId = sourceAccountId,
        destinationAccountId = destinationAccountId,
        title = title,
        note = note,
        date = date
    )
}