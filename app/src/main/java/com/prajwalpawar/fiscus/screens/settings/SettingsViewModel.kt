package com.prajwalpawar.fiscus.screens.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.prajwalpawar.fiscus.data.repository.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel (app: Application): AndroidViewModel(app) {
    private val repo = SettingsRepository(app)

    val darkTheme = repo.darkTheme.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        false
    )

    val appBarStyle = repo.appBarStyle.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        "LARGE"
    )

    fun toggleDarkTheme(value: Boolean) {
        viewModelScope.launch {
            repo.setDarkTheme(value)
        }
    }

    fun setAppBarStyle(style: String) {
        viewModelScope.launch {
            repo.setAppBarStyle(style)
        }
    }
}