package com.expensetracker.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val colorHex: String,
    val iconKey: String,
    val monthlyBudget: Double = 0.0,
    val isDefault: Boolean = false
)

// Curated default categories created on first launch (not "mock data" — these are
// real usable categories, same as any finance app ships with).
val DEFAULT_CATEGORIES = listOf(
    Category(name = "Food & Dining", colorHex = "#FF6B4A", iconKey = "restaurant"),
    Category(name = "Transport", colorHex = "#4A90FF", iconKey = "directions_car"),
    Category(name = "Shopping", colorHex = "#B26AFF", iconKey = "shopping_bag"),
    Category(name = "Bills & Utilities", colorHex = "#FF4A8D", iconKey = "receipt_long"),
    Category(name = "Entertainment", colorHex = "#FFA53E", iconKey = "movie"),
    Category(name = "Health", colorHex = "#2FBF71", iconKey = "health_and_safety"),
    Category(name = "Groceries", colorHex = "#4ABF8C", iconKey = "local_grocery_store"),
    Category(name = "Rent & Housing", colorHex = "#4A6BFF", iconKey = "home"),
    Category(name = "Education", colorHex = "#3EC6E0", iconKey = "school"),
    Category(name = "Salary", colorHex = "#2FBF71", iconKey = "payments"),
    Category(name = "Pocket Money", colorHex = "#4A6BFF", iconKey = "account_balance_wallet"),
    Category(name = "Gifts", colorHex = "#FF4A8D", iconKey = "redeem"),
    Category(name = "Refunds", colorHex = "#4ABF8C", iconKey = "settings_backup_restore"),
    Category(name = "Debt", colorHex = "#FF4A4A", iconKey = "history_edu"),
    Category(name = "Lend", colorHex = "#4A90FF", iconKey = "volunteer_activism"),
    Category(name = "Other", colorHex = "#8A8F98", iconKey = "category")
)
