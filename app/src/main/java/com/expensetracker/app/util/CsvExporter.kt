package com.expensetracker.app.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.expensetracker.app.data.Account
import com.expensetracker.app.data.Category
import com.expensetracker.app.data.Expense
import com.expensetracker.app.data.TransactionType
import java.io.File
import java.io.FileWriter

object CsvExporter {
    fun export(context: Context, expenses: List<Expense>, categories: List<Category>, accounts: List<Account>): Uri? {
        return try {
            val categoryMap = categories.associateBy { it.id }
            val accountMap = accounts.associateBy { it.id }
            val dir = File(context.cacheDir, "exports").apply { mkdirs() }
            val file = File(dir, "expenses_${System.currentTimeMillis()}.csv")
            FileWriter(file).use { writer ->
                writer.append("Date,Title,Category,Account,Amount,Type,Note\n")
                expenses.forEach { e ->
                    val catName = categoryMap[e.categoryId]?.name ?: "Uncategorized"
                    val accName = accountMap[e.accountId]?.name ?: "None"
                    val date = DateUtils.formatDate(e.dateMillis, "yyyy-MM-dd")
                    val title = e.title.replace(",", " ")
                    val note = e.note.replace(",", " ").replace("\n", " ")
                    writer.append("$date,$title,$catName,$accName,${e.amount},${e.type.name},$note\n")
                }
            }
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } catch (ex: Exception) {
            null
        }
    }

    fun shareIntent(context: Context, uri: android.net.Uri): Intent {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/csv"
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        return Intent.createChooser(intent, "Export expenses")
    }
}
