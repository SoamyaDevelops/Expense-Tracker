package com.expensetracker.app.util

import android.content.Context
import android.net.Uri
import com.expensetracker.app.data.Expense
import com.expensetracker.app.data.ExpenseRepository
import com.expensetracker.app.data.TransactionType
import java.io.BufferedReader
import java.io.InputStreamReader

object CsvImporter {
    suspend fun import(context: Context, uri: Uri, repository: ExpenseRepository): Int {
        var count = 0
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val reader = BufferedReader(InputStreamReader(inputStream))
                val allLines = reader.readLines()
                if (allLines.isEmpty()) return 0
                
                // Skip header (first line) and reverse the rest
                val dataLines = allLines.drop(1).reversed()
                
                for (line in dataLines) {
                    val parts = line.split(",")
                    if (parts.size >= 6) {
                        val dateStr = parts[0]
                        val title = parts[1]
                        val categoryName = parts[2]
                        val accountName = parts[3]
                        val amount = parts[4].toDoubleOrNull() ?: 0.0
                        val typeStr = parts[5]
                        val note = if (parts.size > 6) parts[6] else ""
                        
                        val dateMillis = DateUtils.parseDate(dateStr) ?: System.currentTimeMillis()
                        val category = repository.getCategoryByName(categoryName)
                        val account = repository.getAccountByName(accountName)
                        val type = runCatching { TransactionType.valueOf(typeStr) }.getOrDefault(TransactionType.EXPENSE)
                        
                        val expense = Expense(
                            title = title,
                            amount = amount,
                            type = type,
                            categoryId = category?.id,
                            accountId = account?.id,
                            dateMillis = dateMillis,
                            note = note
                        )
                        repository.addExpense(expense)
                        count++
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return count
    }
}
