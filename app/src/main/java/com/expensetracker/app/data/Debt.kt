package com.expensetracker.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class DebtType { BORROWED, LENT }

@Entity(tableName = "debts")
data class Debt(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val personName: String,
    val amount: Double,
    val type: DebtType,
    val dateMillis: Long = System.currentTimeMillis(),
    val note: String = "",
    val isResolved: Boolean = false,
    val linkedExpenseId: Long? = null
)
