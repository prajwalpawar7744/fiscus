package com.prajwalpawar.fiscus.ui.screens.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

// The add-transaction FAB lives in the app Scaffold, so it is placed and inset correctly.
@Composable
fun HomeScreen (
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize()
    )
}
