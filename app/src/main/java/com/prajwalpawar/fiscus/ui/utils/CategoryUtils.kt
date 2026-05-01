package com.prajwalpawar.fiscus.ui.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ElectricalServices
import androidx.compose.material.icons.filled.FoodBank
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Work
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
        "receiptlong", "bill", "invoice" -> Icons.AutoMirrored.Filled.ReceiptLong
        "work", "business" -> Icons.Default.Work
        "redeem", "gift" -> Icons.Default.Redeem
        "trendingup", "invest", "chart" -> Icons.AutoMirrored.Filled.TrendingUp
        "category", "other" -> Icons.Default.Category
        else -> Icons.Default.Category
    }
}
