package com.expensetracker.app.data

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromRecurrence(value: RecurrenceType): String = value.name

    @TypeConverter
    fun toRecurrence(value: String): RecurrenceType =
        runCatching { RecurrenceType.valueOf(value) }.getOrDefault(RecurrenceType.NONE)

    @TypeConverter
    fun fromTransactionType(value: TransactionType): String = value.name

    @TypeConverter
    fun toTransactionType(value: String): TransactionType =
        runCatching { TransactionType.valueOf(value) }.getOrDefault(TransactionType.EXPENSE)

    @TypeConverter
    fun fromDebtType(value: DebtType): String = value.name

    @TypeConverter
    fun toDebtType(value: String): DebtType =
        runCatching { DebtType.valueOf(value) }.getOrDefault(DebtType.BORROWED)
}
