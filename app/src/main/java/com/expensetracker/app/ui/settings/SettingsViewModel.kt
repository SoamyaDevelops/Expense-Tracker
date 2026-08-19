package com.expensetracker.app.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.expensetracker.app.ExpenseApp
import com.expensetracker.app.data.LockType
import com.expensetracker.app.data.ThemeMode
import com.expensetracker.app.util.CsvImporter
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val lockType: LockType = LockType.NONE,
    val biometricEnabled: Boolean = false,
    val currencySymbol: String = "$",
    val monthlyBudget: Double = 0.0
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val settings = (application as ExpenseApp).settingsRepository
    private val repo = (application as ExpenseApp).repository

    val uiState: StateFlow<SettingsUiState> = combine(
        settings.themeMode, settings.lockType, settings.biometricEnabled, settings.currencySymbol, settings.monthlyBudgetTotal
    ) { theme, lock, biometric, currency, budget ->
        SettingsUiState(theme, lock, biometric, currency, budget)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsUiState())

    fun setThemeMode(mode: ThemeMode) { viewModelScope.launch { settings.setThemeMode(mode) } }
    fun setLock(type: LockType, hash: String?, biometric: Boolean) {
        viewModelScope.launch {
            settings.setLock(type, hash)
            settings.setBiometricEnabled(biometric && type != LockType.NONE)
        }
    }
    fun setBiometric(enabled: Boolean) { viewModelScope.launch { settings.setBiometricEnabled(enabled) } }
    fun setCurrency(symbol: String) { viewModelScope.launch { settings.setCurrencySymbol(symbol) } }
    fun setMonthlyBudget(amount: Double) { viewModelScope.launch { settings.setMonthlyBudgetTotal(amount) } }

    suspend fun exportCsv(): Triple<List<com.expensetracker.app.data.Expense>, List<com.expensetracker.app.data.Category>, List<com.expensetracker.app.data.Account>> {
        val expenses = repo.getAllExpenses().first()
        val categories = repo.getAllCategories().first()
        val accounts = repo.getAllAccounts().first()
        return Triple(expenses, categories, accounts)
    }

    suspend fun importCsv(uri: android.net.Uri): Int {
        return CsvImporter.import(getApplication(), uri, repo)
    }
}
