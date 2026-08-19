package com.expensetracker.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class AccountType { CASH, BANK, UPI }

@Entity(tableName = "accounts")
data class Account(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val balance: Double,
    val type: AccountType
)

val DEFAULT_ACCOUNTS = listOf(
    Account(name = "Cash", balance = 0.0, type = AccountType.CASH),
    Account(name = "Bank Account", balance = 0.0, type = AccountType.BANK),
    Account(name = "UPI 1", balance = 0.0, type = AccountType.UPI)
)
