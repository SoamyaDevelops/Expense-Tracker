package com.expensetracker.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.fragment.app.FragmentActivity
import com.expensetracker.app.data.ThemeMode
import com.expensetracker.app.ui.AppNavHost
import com.expensetracker.app.ui.lock.LockScreen
import com.expensetracker.app.ui.theme.ExpenseTrackerTheme

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val app = application as ExpenseApp
            val themeMode by app.settingsRepository.themeMode.collectAsState(initial = ThemeMode.SYSTEM)
            val darkTheme = when (themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            ExpenseTrackerTheme(darkTheme = darkTheme) {
                AppRoot()
            }
        }
    }
}

@Composable
private fun AppRoot() {
    val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as ExpenseApp
    val lockType by app.settingsRepository.lockType.collectAsState(initial = com.expensetracker.app.data.LockType.NONE)
    val lockHash by app.settingsRepository.lockCodeHash.collectAsState(initial = null)
    val biometricEnabled by app.settingsRepository.biometricEnabled.collectAsState(initial = false)
    var unlocked by remember { mutableStateOf(false) }

    AnimatedContent(
        targetState = unlocked || lockType == com.expensetracker.app.data.LockType.NONE,
        transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(200)) },
        label = "lock_transition"
    ) { isUnlocked ->
        if (isUnlocked) {
            AppNavHost()
        } else {
            LockScreen(
                lockType = lockType,
                correctHash = lockHash,
                biometricEnabled = biometricEnabled,
                onUnlocked = { unlocked = true }
            )
        }
    }
}
