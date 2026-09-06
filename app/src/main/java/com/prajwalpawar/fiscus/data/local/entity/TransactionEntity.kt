package com.prajwalpawar.fiscus.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.prajwalpawar.fiscus.data.model.Transaction
import com.prajwalpawar.fiscus.data.model.TransactionType

@Entity(tableName = "transactions")
data class TransactionEntity (
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val type: String,
    val amount: Long,
    val category: String,
    val account: String,
    val note: String?,
    val date: Long
)

fun TransactionEntity.toTransaction(): Transaction {
    return Transaction(
        id = id,
        type = TransactionType.valueOf(type),
        amount = amount,
        category = category,
        account = account,
        note = note,
        date = date
    )
}

fun Transaction.toEntity(): TransactionEntity {
    return TransactionEntity(
        id = id,
        type = type.name,
        amount = amount,
        category = category,
        account = account,
        note = note,
        date = date
    )
}