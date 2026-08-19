package com.expensetracker.app

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.expensetracker.app.data.AppDatabase
import com.expensetracker.app.data.ExpenseRepository
import com.expensetracker.app.data.SettingsRepository
import com.expensetracker.app.util.AutoPayWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import java.util.concurrent.TimeUnit

class ExpenseApp : Application() {
    val applicationScope = CoroutineScope(SupervisorJob())

    val database by lazy { AppDatabase.getInstance(this, applicationScope) }
    val repository by lazy { 
        ExpenseRepository(
            database.expenseDao(), 
            database.categoryDao(),
            database.accountDao(),
            database.transferDao(),
            database.debtDao()
        ) 
    }
    val settingsRepository by lazy { SettingsRepository(this) }

    override fun onCreate() {
        super.onCreate()
        scheduleAutoPayReminders()
    }

    private fun scheduleAutoPayReminders() {
        val workRequest = PeriodicWorkRequestBuilder<AutoPayWorker>(12, TimeUnit.HOURS)
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "AutoPayReminders",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}
