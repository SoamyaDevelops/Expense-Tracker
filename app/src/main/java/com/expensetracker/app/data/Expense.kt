package com.expensetracker.app.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class RecurrenceType { NONE, DAILY, WEEKLY, MONTHLY, YEARLY }
enum class TransactionType { EXPENSE, INCOME }

@Entity(
    tableName = "expenses",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("categoryId"), Index("dateMillis"), Index("accountId")]
)
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val amount: Double,
    val type: TransactionType = TransactionType.EXPENSE,
    val categoryId: Long?,
    val accountId: Long? = null,
    val dateMillis: Long,
    val note: String = "",
    val recurrence: RecurrenceType = RecurrenceType.NONE,
    val isAutoPay: Boolean = false,
    val isFavorite: Boolean = false,
    val createdAtMillis: Long = System.currentTimeMillis()
)
