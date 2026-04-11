package com.prajwalpawar.fiscus.domain.model

import java.util.Date
import kotlinx.serialization.Serializable
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.text.SimpleDateFormat
import java.util.Locale

object DateSerializer : KSerializer<Date> {
    private val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US)

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Date", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Date) {
        encoder.encodeString(format.format(value))
    }

    override fun deserialize(decoder: Decoder): Date {
        return format.parse(decoder.decodeString()) ?: Date()
    }
}

@Serializable
enum class TransactionType {
    INCOME, EXPENSE, TRANSFER
}

@Serializable
data class Transaction(
    val id: Long? = null,
    val title: String,
    val amount: Double,
    val type: TransactionType,
    val categoryId: Long,
    val accountId: Long,
    val toAccountId: Long? = null,
    @Serializable(with = DateSerializer::class)
    val date: Date,
    val note: String = ""
)

@Serializable
data class Category(
    val id: Long? = null,
    val name: String,
    val icon: String,
    val color: Int,
    val type: TransactionType? = null
)

@Serializable
data class Account(
    val id: Long? = null,
    val name: String,
    val balance: Double, // This can be initial balance
    val icon: String
)

data class AccountWithBalance(
    val account: Account,
    val balance: Double
)
