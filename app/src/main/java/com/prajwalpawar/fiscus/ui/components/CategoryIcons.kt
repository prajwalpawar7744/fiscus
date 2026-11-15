package com.prajwalpawar.fiscus.ui.components

import androidx.annotation.DrawableRes
import com.prajwalpawar.fiscus.R

/**
 * Maps a category's `iconKey` (a Material Symbols name, ADR-016) to its Rounded drawable
 * (ADR-010). Unknown keys fall back to the generic "category" symbol.
 */
@DrawableRes
fun categoryIconRes(iconKey: String): Int = when (iconKey) {
    "restaurant" -> R.drawable.ic_restaurant_rounded
    // "local_grocery_store" is an alias of "shopping_cart" in Material Symbols.
    "local_grocery_store", "shopping_cart" -> R.drawable.ic_shopping_cart_rounded
    "directions_car" -> R.drawable.ic_directions_car_rounded
    "receipt_long" -> R.drawable.ic_receipt_long_rounded
    "shopping_bag" -> R.drawable.ic_shopping_bag_rounded
    "medical_services" -> R.drawable.ic_medical_services_rounded
    "movie" -> R.drawable.ic_movie_rounded
    "school" -> R.drawable.ic_school_rounded
    "work" -> R.drawable.ic_work_rounded
    "storefront" -> R.drawable.ic_storefront_rounded
    "redeem" -> R.drawable.ic_redeem_rounded
    "payments" -> R.drawable.ic_payments_rounded
    else -> R.drawable.ic_category_rounded
}
