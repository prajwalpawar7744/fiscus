package com.prajwalpawar.fiscus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.prajwalpawar.fiscus.screens.settings.SettingsViewModel
import com.prajwalpawar.fiscus.ui.theme.FiscusTheme
import androidx.compose.runtime.getValue

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settingsViewModel: SettingsViewModel = viewModel()
            val darkTheme by settingsViewModel.darkTheme.collectAsState()

            FiscusTheme (
                darkTheme = darkTheme
            ) {
                FiscusApp()
            }
        }
    }
}