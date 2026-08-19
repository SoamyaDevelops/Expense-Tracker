package com.expensetracker.app.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.lifecycle.viewmodel.compose.viewModel
import com.expensetracker.app.ui.addedit.AddEditExpenseScreen
import com.expensetracker.app.ui.accounts.AccountsScreen
import com.expensetracker.app.ui.categories.CategoriesScreen
import com.expensetracker.app.ui.debts.DebtScreen
import com.expensetracker.app.ui.home.HomeScreen
import com.expensetracker.app.ui.lock.SetupLockScreen
import com.expensetracker.app.ui.settings.SettingsScreen
import com.expensetracker.app.ui.settings.SettingsViewModel
import com.expensetracker.app.ui.stats.StatsScreen

object Routes {
    const val HOME = "home"
    const val ADD_EXPENSE = "add_expense"
    const val EDIT_EXPENSE = "edit_expense/{id}"
    const val STATS = "stats"
    const val CATEGORIES = "categories"
    const val ACCOUNTS = "accounts"
    const val DEBTS = "debts"
    const val SETTINGS = "settings"
    const val LOCK_SETUP = "lock_setup"

    fun editExpense(id: Long) = "edit_expense/$id"
}

private const val ANIM_DURATION = 260

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(ANIM_DURATION)
            ) + fadeIn(animationSpec = tween(ANIM_DURATION))
        },
        exitTransition = {
            fadeOut(animationSpec = tween(ANIM_DURATION))
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(ANIM_DURATION))
        },
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(ANIM_DURATION)
            ) + fadeOut(animationSpec = tween(ANIM_DURATION))
        }
    ) {
        composable(Routes.HOME) {
            HomeScreen(
                onAddExpense = { navController.navigate(Routes.ADD_EXPENSE) },
                onEditExpense = { id -> navController.navigate(Routes.editExpense(id)) },
                onOpenStats = { navController.navigate(Routes.STATS) },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                onOpenCategories = { navController.navigate(Routes.CATEGORIES) },
                onOpenAccounts = { navController.navigate(Routes.ACCOUNTS) },
                onOpenDebts = { navController.navigate(Routes.DEBTS) }
            )
        }
        composable(Routes.ADD_EXPENSE) {
            AddEditExpenseScreen(expenseId = null, onDone = { navController.popBackStack() })
        }
        composable(
            Routes.EDIT_EXPENSE,
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("id") ?: 0L
            AddEditExpenseScreen(expenseId = id, onDone = { navController.popBackStack() })
        }
        composable(Routes.STATS) {
            StatsScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.CATEGORIES) {
            CategoriesScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.ACCOUNTS) {
            AccountsScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.DEBTS) {
            DebtScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onOpenLockSetup = { navController.navigate(Routes.LOCK_SETUP) }
            )
        }
        composable(Routes.LOCK_SETUP) {
            val settingsViewModel: SettingsViewModel = viewModel()
            val state by settingsViewModel.uiState.collectAsState()
            SetupLockScreen(
                currentLockType = state.lockType,
                biometricEnabled = state.biometricEnabled,
                onSave = { type, hash, biometric ->
                    settingsViewModel.setLock(type, hash, biometric)
                    navController.popBackStack()
                },
                onBiometricToggle = { settingsViewModel.setBiometric(it) },
                onBack = { navController.popBackStack() }
            )
        }
    }
}
