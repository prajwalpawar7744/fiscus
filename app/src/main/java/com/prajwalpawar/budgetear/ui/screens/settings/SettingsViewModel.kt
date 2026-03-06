package com.prajwalpawar.budgetear.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prajwalpawar.budgetear.data.local.pref.PreferenceManager
import com.prajwalpawar.budgetear.domain.repository.BudgetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val userName: String = "",
    val userPhotoUri: String? = null,
    val themeMode: String = "system",
    val isBiometricEnabled: Boolean = false,
    val currency: String = "USD"
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferenceManager: PreferenceManager,
    private val repository: BudgetRepository
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        preferenceManager.userName,
        preferenceManager.userPhotoUri,
        preferenceManager.themeMode,
        preferenceManager.isBiometricEnabled,
        preferenceManager.currency
    ) { name, photo, theme, biometric, currency ->
        SettingsUiState(name, photo, theme, biometric, currency)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsUiState())

    fun updateUserName(name: String) {
        viewModelScope.launch {
            preferenceManager.updateUserName(name)
        }
    }

    fun updateUserPhotoUri(uri: String) {
        viewModelScope.launch {
            preferenceManager.updateUserPhotoUri(uri)
        }
    }

    fun updateThemeMode(mode: String) {
        viewModelScope.launch {
            preferenceManager.updateThemeMode(mode)
        }
    }

    fun updateBiometricEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferenceManager.updateBiometricEnabled(enabled)
        }
    }

    fun updateCurrency(currency: String) {
        viewModelScope.launch {
            preferenceManager.updateCurrency(currency)
        }
    }

    fun resetData() {
        viewModelScope.launch {
            repository.clearAllData()
        }
    }
}
