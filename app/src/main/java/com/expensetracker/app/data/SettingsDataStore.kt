package com.expensetracker.app.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "settings")

enum class ThemeMode { SYSTEM, LIGHT, DARK }
enum class LockType { NONE, PIN, PASSWORD, PATTERN }

class SettingsRepository(private val context: Context) {

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val LOCK_TYPE = stringPreferencesKey("lock_type")
        val LOCK_CODE_HASH = stringPreferencesKey("lock_code_hash")
        val BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
        val CURRENCY_SYMBOL = stringPreferencesKey("currency_symbol")
        val FIRST_LAUNCH_DONE = booleanPreferencesKey("first_launch_done")
        val MONTHLY_BUDGET_TOTAL = stringPreferencesKey("monthly_budget_total")
    }

    val themeMode: Flow<ThemeMode> = context.dataStore.data.map { prefs ->
        runCatching { ThemeMode.valueOf(prefs[Keys.THEME_MODE] ?: ThemeMode.SYSTEM.name) }
            .getOrDefault(ThemeMode.SYSTEM)
    }

    val lockType: Flow<LockType> = context.dataStore.data.map { prefs ->
        runCatching { LockType.valueOf(prefs[Keys.LOCK_TYPE] ?: LockType.NONE.name) }
            .getOrDefault(LockType.NONE)
    }

    val lockCodeHash: Flow<String?> = context.dataStore.data.map { it[Keys.LOCK_CODE_HASH] }
    val biometricEnabled: Flow<Boolean> = context.dataStore.data.map { it[Keys.BIOMETRIC_ENABLED] ?: false }
    val currencySymbol: Flow<String> = context.dataStore.data.map { it[Keys.CURRENCY_SYMBOL] ?: "$" }
    val firstLaunchDone: Flow<Boolean> = context.dataStore.data.map { it[Keys.FIRST_LAUNCH_DONE] ?: false }
    val monthlyBudgetTotal: Flow<Double> = context.dataStore.data.map {
        it[Keys.MONTHLY_BUDGET_TOTAL]?.toDoubleOrNull() ?: 0.0
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    suspend fun setLock(type: LockType, codeHash: String?) {
        context.dataStore.edit {
            it[Keys.LOCK_TYPE] = type.name
            if (codeHash != null) it[Keys.LOCK_CODE_HASH] = codeHash
            if (type == LockType.NONE) {
                it.remove(Keys.LOCK_CODE_HASH)
                it[Keys.BIOMETRIC_ENABLED] = false
            }
        }
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.BIOMETRIC_ENABLED] = enabled }
    }

    suspend fun setCurrencySymbol(symbol: String) {
        context.dataStore.edit { it[Keys.CURRENCY_SYMBOL] = symbol }
    }

    suspend fun setFirstLaunchDone() {
        context.dataStore.edit { it[Keys.FIRST_LAUNCH_DONE] = true }
    }

    suspend fun setMonthlyBudgetTotal(amount: Double) {
        context.dataStore.edit { it[Keys.MONTHLY_BUDGET_TOTAL] = amount.toString() }
    }
}
