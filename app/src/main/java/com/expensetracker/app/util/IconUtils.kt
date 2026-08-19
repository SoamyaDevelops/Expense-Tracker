package com.expensetracker.app.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

object IconUtils {
    fun getCategoryIcon(key: String): ImageVector = when (key) {
        "restaurant" -> Icons.Outlined.Restaurant
        "directions_car" -> Icons.Outlined.DirectionsCar
        "shopping_bag" -> Icons.Outlined.ShoppingBag
        "receipt_long" -> Icons.AutoMirrored.Outlined.ReceiptLong
        "movie" -> Icons.Outlined.Movie
        "health_and_safety" -> Icons.Outlined.HealthAndSafety
        "local_grocery_store" -> Icons.Outlined.LocalGroceryStore
        "home" -> Icons.Outlined.Home
        "school" -> Icons.Outlined.School
        "payments" -> Icons.Outlined.Payments
        "account_balance_wallet" -> Icons.Outlined.AccountBalanceWallet
        "redeem" -> Icons.Outlined.Redeem
        "settings_backup_restore" -> Icons.Outlined.SettingsBackupRestore
        "history_edu" -> Icons.Outlined.HistoryEdu
        "volunteer_activism" -> Icons.Outlined.VolunteerActivism
        else -> Icons.Outlined.Category
    }

    fun getAccountIcon(type: String): ImageVector = when (type.uppercase()) {
        "CASH" -> Icons.Outlined.Payments
        "BANK" -> Icons.Outlined.AccountBalance
        "UPI" -> Icons.Outlined.QrCodeScanner
        else -> Icons.Outlined.AccountBalanceWallet
    }
}
