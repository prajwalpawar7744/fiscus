package com.prajwalpawar.fiscus.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Shape
import androidx.graphics.shapes.RoundedPolygon
import com.prajwalpawar.fiscus.data.model.IconShapePreference

internal val LocalIconShape = staticCompositionLocalOf<Shape> { CircleShape }

object FiscusShapes {
    // The user's chosen shape for decorative icon containers (list badges,
    // empty states, previews), so they all match.
    val icon: Shape
        @Composable
        @ReadOnlyComposable
        get() = LocalIconShape.current
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun IconShapePreference.polygon(): RoundedPolygon {
    return when (this) {
        IconShapePreference.CIRCLE -> MaterialShapes.Circle
        IconShapePreference.SQUARE -> MaterialShapes.Square
        IconShapePreference.COOKIE -> MaterialShapes.Cookie9Sided
        IconShapePreference.CLOVER -> MaterialShapes.Clover4Leaf
        IconShapePreference.SUNNY -> MaterialShapes.Sunny
        IconShapePreference.FLOWER -> MaterialShapes.Flower
    }
}
