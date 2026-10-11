package com.prajwalpawar.fiscus.data.model

data class Account (
    val id: Long = 0L,
    val name: String,
    val type: AccountType,
    val openingBalance: Long = 0L,
    val isDefault: Boolean = false,
    val isArchived: Boolean = false
)