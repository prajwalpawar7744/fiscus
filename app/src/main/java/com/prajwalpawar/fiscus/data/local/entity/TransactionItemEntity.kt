package com.prajwalpawar.fiscus.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.prajwalpawar.fiscus.data.model.TransactionItem

// One line of an itemised transaction. Added in schema 2; deleted with its transaction.
@Entity(
    tableName = "transaction_items",
    foreignKeys = [
        ForeignKey(
            entity = TransactionEntity::class,
            parentColumns = ["id"],
            childColumns = ["transactionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("transactionId")]
)
data class TransactionItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val transactionId: Long,
    // Order the items were entered in.
    val position: Int,
    val name: String,
    // Hundredths, so 1.5 kg is 150.
    val quantity: Long,
    // Hundredths of the currency unit, like every amount.
    val unitPrice: Long
)

fun TransactionItemEntity.toItem(): TransactionItem =
    TransactionItem(name = name, quantity = quantity, unitPrice = unitPrice)

fun TransactionItem.toEntity(transactionId: Long, position: Int): TransactionItemEntity =
    TransactionItemEntity(
        transactionId = transactionId,
        position = position,
        name = name,
        quantity = quantity,
        unitPrice = unitPrice
    )
