package com.expensetracker.app.util

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.expensetracker.app.ExpenseApp
import kotlinx.coroutines.flow.first
import java.util.Calendar
import java.util.concurrent.TimeUnit

class AutoPayWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val repo = (applicationContext as ExpenseApp).repository
        val expenses = repo.getAllExpenses().first()
        
        expenses.filter { it.isAutoPay }.forEach { expense ->
            val expenseCal = Calendar.getInstance().apply { timeInMillis = expense.dateMillis }
            val nowCal = Calendar.getInstance()
            
            // Normalize to start of day for comparison
            expenseCal.set(Calendar.HOUR_OF_DAY, 0)
            expenseCal.set(Calendar.MINUTE, 0)
            expenseCal.set(Calendar.SECOND, 0)
            expenseCal.set(Calendar.MILLISECOND, 0)
            
            nowCal.set(Calendar.HOUR_OF_DAY, 0)
            nowCal.set(Calendar.MINUTE, 0)
            nowCal.set(Calendar.SECOND, 0)
            nowCal.set(Calendar.MILLISECOND, 0)
            
            val diff = expenseCal.timeInMillis - nowCal.timeInMillis
            val daysUntil = TimeUnit.MILLISECONDS.toDays(diff)
            
            if (daysUntil == 3L) {
                NotificationHelper.showReminderNotification(
                    applicationContext,
                    "Upcoming Auto Pay",
                    "Payment for '${expense.title}' of ${expense.amount} is due in 3 days."
                )
            } else if (daysUntil == 1L) {
                NotificationHelper.showReminderNotification(
                    applicationContext,
                    "Payment Tomorrow",
                    "Your auto pay for '${expense.title}' is due tomorrow."
                )
            }
        }

        return Result.success()
    }
}
