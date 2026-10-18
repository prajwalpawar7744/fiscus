package com.prajwalpawar.fiscus.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Business
import androidx.compose.material.icons.rounded.CardGiftcard
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Receipt
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.ui.graphics.vector.ImageVector
import com.prajwalpawar.fiscus.data.model.AccountType

// Icons a category can use, keyed by the iconKey stored in the database.
val categoryIconOptions: Map<String, ImageVector> = linkedMapOf(
    "category" to Icons.Rounded.Category,
    "restaurant" to Icons.Rounded.Restaurant,
    "directions_car" to Icons.Rounded.DirectionsCar,
    "shopping_bag" to Icons.Rounded.ShoppingBag,
    "receipt" to Icons.Rounded.Receipt,
    "health" to Icons.Rounded.Favorite,
    "school" to Icons.Rounded.School,
    "movie" to Icons.Rounded.Movie,
    "home" to Icons.Rounded.Home,
    "payments" to Icons.Rounded.Payments,
    "business" to Icons.Rounded.Business,
    "trending_up" to Icons.Rounded.TrendingUp,
    "card_giftcard" to Icons.Rounded.CardGiftcard
)

fun categoryIcon(iconKey: String): ImageVector {
    return categoryIconOptions[iconKey] ?: Icons.Rounded.Category
}

fun accountIcon(type: AccountType): ImageVector {
    return when (type) {
        AccountType.CASH -> Icons.Rounded.Payments
        AccountType.BANK -> Icons.Rounded.AccountBalance
        AccountType.CREDIT_CARD -> Icons.Rounded.CreditCard
        AccountType.E_WALLET -> Icons.Rounded.AccountBalanceWallet
        AccountType.OTHER -> Icons.Rounded.Savings
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
