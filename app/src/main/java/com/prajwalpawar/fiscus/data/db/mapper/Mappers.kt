package com.prajwalpawar.fiscus.data.db.mapper

import com.prajwalpawar.fiscus.data.db.entity.AccountEntity
import com.prajwalpawar.fiscus.data.db.entity.CategoryEntity
import com.prajwalpawar.fiscus.data.db.entity.TransactionEntity
import com.prajwalpawar.fiscus.model.Account
import com.prajwalpawar.fiscus.model.Category
import com.prajwalpawar.fiscus.model.Money
import com.prajwalpawar.fiscus.model.Transaction
import kotlin.time.Instant

// Entity <-> model mapping. Entities never leave the data layer (ARCHITECTURE.md).

fun AccountEntity.asModel() = Account(
    id = id,
    name = name,
    type = type,
    currencyCode = currencyCode,
    openingBalance = Money(openingBalanceMinor, currencyCode),
    archived = archived,
    isDefault = isDefault,
)

fun Account.asEntity() = AccountEntity(
    id = id,
    name = name,
    type = type,
    currencyCode = currencyCode,
    openingBalanceMinor = openingBalance.amountMinor,
    archived = archived,
    isDefault = isDefault,
)

fun CategoryEntity.asModel() = Category(id = id, name = name, kind = kind, iconKey = iconKey, archived = archived)

fun Category.asEntity() = CategoryEntity(id = id, name = name, kind = kind, iconKey = iconKey, archived = archived)

fun TransactionEntity.asModel() = Transaction(
    id = id,
    type = type,
    title = title,
    amount = Money(amountMinor, currencyCode),
    accountId = accountId,
    toAccountId = toAccountId,
    categoryId = categoryId,
    note = note,
    occurredAt = Instant.fromEpochMilliseconds(occurredAtMillis),
    createdAt = Instant.fromEpochMilliseconds(createdAtMillis),
    updatedAt = Instant.fromEpochMilliseconds(updatedAtMillis),
)

fun Transaction.asEntity() = TransactionEntity(
    id = id,
    type = type,
    title = title,
    amountMinor = amount.amountMinor,
    currencyCode = amount.currencyCode,
    accountId = accountId,
    toAccountId = toAccountId,
    categoryId = categoryId,
    note = note,
    occurredAtMillis = occurredAt.toEpochMilliseconds(),
    createdAtMillis = createdAt.toEpochMilliseconds(),
    updatedAtMillis = updatedAt.toEpochMilliseconds(),
)
