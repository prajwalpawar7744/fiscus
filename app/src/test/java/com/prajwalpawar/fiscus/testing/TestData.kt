package com.prajwalpawar.fiscus.testing

import com.prajwalpawar.fiscus.model.Account
import com.prajwalpawar.fiscus.model.AccountType
import com.prajwalpawar.fiscus.model.Category
import com.prajwalpawar.fiscus.model.CategoryKind
import com.prajwalpawar.fiscus.model.Money
import kotlin.time.Clock
import kotlin.time.Instant

// Shared fixtures for ViewModel tests.

/** 2026-11-15T10:30:00Z. */
val TestNow: Instant = Instant.parse("2026-11-15T10:30:00Z")

class FixedClock(private val now: Instant = TestNow) : Clock {
    override fun now(): Instant = now
}

val CashAccount = Account(id = 1, name = "Cash", type = AccountType.CASH, currencyCode = "INR", openingBalance = Money(0, "INR"))
val BankAccount = Account(id = 2, name = "Bank", type = AccountType.BANK, currencyCode = "INR", openingBalance = Money(0, "INR"))

val FoodCategory = Category(id = 1, name = "Food", kind = CategoryKind.EXPENSE, iconKey = "restaurant")
val SalaryCategory = Category(id = 2, name = "Salary", kind = CategoryKind.INCOME, iconKey = "work")
