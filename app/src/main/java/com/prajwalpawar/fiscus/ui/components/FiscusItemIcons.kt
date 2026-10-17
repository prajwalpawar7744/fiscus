package com.prajwalpawar.fiscus.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.ui.graphics.vector.ImageVector
import com.prajwalpawar.fiscus.data.model.AccountType

// Icons a category can use, keyed by the iconKey stored in the database.
val categoryIconOptions: Map<String, ImageVector> = linkedMapOf(
    "category" to Icons.Filled.Category,
    "restaurant" to Icons.Filled.Restaurant,
    "directions_car" to Icons.Filled.DirectionsCar,
    "shopping_bag" to Icons.Filled.ShoppingBag,
    "receipt" to Icons.Filled.Receipt,
    "health" to Icons.Filled.Favorite,
    "school" to Icons.Filled.School,
    "movie" to Icons.Filled.Movie,
    "home" to Icons.Filled.Home,
    "payments" to Icons.Filled.Payments,
    "business" to Icons.Filled.Business,
    "trending_up" to Icons.Filled.TrendingUp,
    "card_giftcard" to Icons.Filled.CardGiftcard
)

fun categoryIcon(iconKey: String): ImageVector {
    return categoryIconOptions[iconKey] ?: Icons.Filled.Category
}

fun accountIcon(type: AccountType): ImageVector {
    return when (type) {
        AccountType.CASH -> Icons.Filled.Payments
        AccountType.BANK -> Icons.Filled.AccountBalance
        AccountType.CREDIT_CARD -> Icons.Filled.CreditCard
        AccountType.E_WALLET -> Icons.Filled.AccountBalanceWallet
        AccountType.OTHER -> Icons.Filled.Savings
    }
}

fun accountTypeLabel(type: AccountType): String {
    return when (type) {
        AccountType.CASH -> "Cash"
        AccountType.BANK -> "Bank"
        AccountType.CREDIT_CARD -> "Credit card"
        AccountType.E_WALLET -> "E-wallet"
        AccountType.OTHER -> "Other"
    }
}
