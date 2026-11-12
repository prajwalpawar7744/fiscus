package com.prajwalpawar.fiscus.data.db.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import com.prajwalpawar.fiscus.model.AccountType

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val type: AccountType,
    val currencyCode: String,
    val openingBalanceMinor: Long,
    val archived: Boolean,
)
