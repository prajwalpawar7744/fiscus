package com.prajwalpawar.fiscus.ui.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Utility function to map icon names to Material Design Icons.
 * Centralizing this ensures consistent rendering across different screens.
 */
fun getCategoryIcon(name: String): ImageVector {
    val normalizedName = name.replace("_", "").replace(" ", "").lowercase()
    return when (normalizedName) {
        "shoppingbag", "shoppingbagfilled" -> Icons.Default.ShoppingBag
        "shoppingcart", "shoppingcartfilled" -> Icons.Default.ShoppingCart
        "restaurant", "dining" -> Icons.Default.Restaurant
        "foodbank" -> Icons.Default.FoodBank
        "directionsbus", "bus" -> Icons.Default.DirectionsBus
        "directionscar", "car" -> Icons.Default.DirectionsCar
        "localhospital", "hospital", "medicalservices", "medical" -> Icons.Default.LocalHospital
        "school", "education" -> Icons.Default.School
        "entertainment", "confirmationnumber", "movie", "ticket" -> Icons.Default.ConfirmationNumber
        "home", "house" -> Icons.Default.Home
        "electricalservices", "electric", "bolt" -> Icons.Default.ElectricalServices
        "savings", "piggy" -> Icons.Default.Savings
        "accountbalance", "bank" -> Icons.Default.AccountBalance
        "accountbalancewallet", "wallet" -> Icons.Default.AccountBalanceWallet
        "creditcard", "card" -> Icons.Default.CreditCard
        "payments", "money" -> Icons.Default.Payments
        "monetizationon", "cash" -> Icons.Default.MonetizationOn
        "swaphoriz", "transfer", "sync" -> Icons.Default.SwapHoriz
        "receiptlong", "bill", "invoice" -> Icons.Default.ReceiptLong
        "work", "business" -> Icons.Default.Work
        "redeem", "gift" -> Icons.Default.Redeem
        "trendingup", "invest", "chart" -> Icons.Default.TrendingUp
        "category", "other" -> Icons.Default.Category
        else -> Icons.Default.Category
    }
}
