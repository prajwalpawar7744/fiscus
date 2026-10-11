package com.prajwalpawar.fiscus.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.prajwalpawar.fiscus.data.model.Account
import com.prajwalpawar.fiscus.data.model.AccountType

@Entity(
    tableName = "accounts",
    indices = [
        Index(
            value = ["name"],
            unique = true
        )
    ]
)
data class AccountEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val type: String,
    val openingBalance: Long = 0L,
    val isDefault: Boolean = false,
    val isArchived: Boolean = false
)

fun AccountEntity.toAccount(): Account {
    return Account(
        id = id,
        name = name,
        type = AccountType.valueOf(type),
        openingBalance = openingBalance,
        isDefault = isDefault,
        isArchived = isArchived
    )
}

fun Account.toEntity(): AccountEntity {
    return AccountEntity(
        id = id,
        name = name,
        type = type.name,
        openingBalance = openingBalance,
        isDefault = isDefault,
        isArchived = isArchived
    )
}