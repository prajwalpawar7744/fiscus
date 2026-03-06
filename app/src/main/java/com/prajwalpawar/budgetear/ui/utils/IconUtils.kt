package com.prajwalpawar.budgetear.ui.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun getCategoryIcon(iconName: String): ImageVector {
    return when (iconName) {
        "restaurant" -> Icons.Default.Restaurant
        "directions_car" -> Icons.Default.DirectionsCar
        "school" -> Icons.Default.School
        "shopping_cart" -> Icons.Default.ShoppingCart
        "medical_services" -> Icons.Default.MedicalServices
        "movie" -> Icons.Default.Movie
        "payments" -> Icons.Default.Payments
        "work" -> Icons.Default.Work
        "redeem" -> Icons.Default.Redeem
        "trending_up" -> Icons.Default.TrendingUp
        else -> Icons.Default.Category
    }
}
