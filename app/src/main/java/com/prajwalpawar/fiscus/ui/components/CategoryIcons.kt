package com.prajwalpawar.fiscus.ui.components

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.prajwalpawar.fiscus.R

/** One pickable category icon: its Material Symbols name (the stored `iconKey`), drawable and label. */
data class CategoryIcon(
    val key: String,
    @DrawableRes val iconRes: Int,
    @StringRes val labelRes: Int,
)

/**
 * The curated icons a category can use, in picker order (ADR-040): Material Symbols Rounded
 * (ADR-010). The label is the icon's accessibility name in the icon-only picker.
 */
val CategoryIcons: List<CategoryIcon> = listOf(
    CategoryIcon("restaurant", R.drawable.ic_restaurant_rounded, R.string.category_icon_restaurant),
    CategoryIcon("local_cafe", R.drawable.ic_local_cafe_rounded, R.string.category_icon_cafe),
    CategoryIcon("shopping_cart", R.drawable.ic_shopping_cart_rounded, R.string.category_icon_groceries),
    CategoryIcon("shopping_bag", R.drawable.ic_shopping_bag_rounded, R.string.category_icon_shopping),
    CategoryIcon("checkroom", R.drawable.ic_checkroom_rounded, R.string.category_icon_clothing),
    CategoryIcon("home", R.drawable.ic_home_rounded, R.string.category_icon_home),
    CategoryIcon("receipt_long", R.drawable.ic_receipt_long_rounded, R.string.category_icon_bills),
    CategoryIcon("bolt", R.drawable.ic_bolt_rounded, R.string.category_icon_electricity),
    CategoryIcon("wifi", R.drawable.ic_wifi_rounded, R.string.category_icon_internet),
    CategoryIcon("phone_iphone", R.drawable.ic_phone_iphone_rounded, R.string.category_icon_phone),
    CategoryIcon("directions_car", R.drawable.ic_directions_car_rounded, R.string.category_icon_transport),
    CategoryIcon("local_gas_station", R.drawable.ic_local_gas_station_rounded, R.string.category_icon_fuel),
    CategoryIcon("flight", R.drawable.ic_flight_rounded, R.string.category_icon_travel),
    CategoryIcon("medical_services", R.drawable.ic_medical_services_rounded, R.string.category_icon_health),
    CategoryIcon("fitness_center", R.drawable.ic_fitness_center_rounded, R.string.category_icon_fitness),
    CategoryIcon("spa", R.drawable.ic_spa_rounded, R.string.category_icon_personal_care),
    CategoryIcon("movie", R.drawable.ic_movie_rounded, R.string.category_icon_entertainment),
    CategoryIcon("sports_esports", R.drawable.ic_sports_esports_rounded, R.string.category_icon_games),
    CategoryIcon("celebration", R.drawable.ic_celebration_rounded, R.string.category_icon_celebrations),
    CategoryIcon("school", R.drawable.ic_school_rounded, R.string.category_icon_education),
    CategoryIcon("child_care", R.drawable.ic_child_care_rounded, R.string.category_icon_kids),
    CategoryIcon("pets", R.drawable.ic_pets_rounded, R.string.category_icon_pets),
    CategoryIcon("build", R.drawable.ic_build_rounded, R.string.category_icon_repairs),
    CategoryIcon("volunteer_activism", R.drawable.ic_volunteer_activism_rounded, R.string.category_icon_charity),
    CategoryIcon("redeem", R.drawable.ic_redeem_rounded, R.string.category_icon_gifts),
    CategoryIcon("work", R.drawable.ic_work_rounded, R.string.category_icon_salary),
    CategoryIcon("storefront", R.drawable.ic_storefront_rounded, R.string.category_icon_business),
    CategoryIcon("payments", R.drawable.ic_payments_rounded, R.string.category_icon_payments),
    CategoryIcon("trending_up", R.drawable.ic_trending_up_rounded, R.string.category_icon_investments),
    CategoryIcon("savings", R.drawable.ic_savings_rounded, R.string.category_icon_savings),
    CategoryIcon("category", R.drawable.ic_category_rounded, R.string.category_icon_other),
)

private val CategoryIconsByKey: Map<String, CategoryIcon> = CategoryIcons.associateBy { it.key }

/** The picker's key for [iconKey]: "local_grocery_store" is an alias of "shopping_cart". */
fun canonicalCategoryIconKey(iconKey: String): String =
    if (iconKey == "local_grocery_store") "shopping_cart" else iconKey

/**
 * Maps a category's `iconKey` (a Material Symbols name, ADR-016) to its Rounded drawable
 * (ADR-010). Unknown keys fall back to the generic "category" symbol.
 */
@DrawableRes
fun categoryIconRes(iconKey: String): Int =
    CategoryIconsByKey[canonicalCategoryIconKey(iconKey)]?.iconRes ?: R.drawable.ic_category_rounded
