package com.prajwalpawar.fiscus.ui.components

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * The app's single [SnackbarHostState] (ADR-020). `FiscusApp` provides it; every screen's `Scaffold`
 * shows it with `snackbarHost = { SnackbarHost(LocalSnackbarHostState.current) }`, so a confirmation
 * ("Expense added") shows on whichever screen is on top after navigation. Screens don't take it as
 * a parameter. The default instance only serves previews and isolated UI tests.
 */
val LocalSnackbarHostState = staticCompositionLocalOf { SnackbarHostState() }
