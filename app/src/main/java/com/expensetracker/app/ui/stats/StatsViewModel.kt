package com.expensetracker.app.ui.stats

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.expensetracker.app.ExpenseApp
import com.expensetracker.app.data.Category
import com.expensetracker.app.data.CategoryTotal
import com.expensetracker.app.data.DayTotal
import com.expensetracker.app.util.DateUtils
import kotlinx.coroutines.flow.*
import java.util.Calendar

data class StatsUiState(
    val categoryTotals: List<Pair<Category, Double>> = emptyList(),
    val dailyTotals: List<Double> = emptyList(),
    val dailyIncomeTotals: List<Double> = emptyList(),
    val totalExpenses: Double = 0.0,
    val totalIncome: Double = 0.0,
    val average: Double = 0.0,
    val currencySymbol: String = "$",
    val monthsAgo: Int = 0
)

class StatsViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = (application as ExpenseApp).repository
    private val settings = (application as ExpenseApp).settingsRepository

    private val monthOffset = MutableStateFlow(0)

    private val totalsFlow = monthOffset.flatMapLatest { offset ->
        repo.getTotalsByCategory(DateUtils.startOfMonth(offset), DateUtils.endOfMonth(offset))
    }
    private val dailyFlow = monthOffset.flatMapLatest { offset ->
        repo.getDailyTotals(DateUtils.startOfMonth(offset), DateUtils.endOfMonth(offset))
    }
    private val totalExpFlow = monthOffset.flatMapLatest { offset ->
        repo.getTotalExpensesBetween(DateUtils.startOfMonth(offset), DateUtils.endOfMonth(offset))
    }
    private val totalIncFlow = monthOffset.flatMapLatest { offset ->
        repo.getTotalIncomeBetween(DateUtils.startOfMonth(offset), DateUtils.endOfMonth(offset))
    }

    val uiState: StateFlow<StatsUiState> = combine(
        totalsFlow, dailyFlow, totalExpFlow, totalIncFlow, repo.getAllCategories(), settings.currencySymbol, monthOffset
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        val totals = values[0] as List<CategoryTotal>
        val daily = values[1] as List<DayTotal>
        val totalExp = values[2] as Double
        val totalInc = values[3] as Double
        val categories = (values[4] as List<Category>).associateBy { it.id }
        val symbol = values[5] as String
        val offset = values[6] as Int

        val categoryTotals = totals
            .mapNotNull { ct -> categories[ct.categoryId]?.let { it to ct.total } }
            .sortedByDescending { it.second }

        val cal = Calendar.getInstance()
        cal.add(Calendar.MONTH, -offset)
        val maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val startDayTimestamp = DateUtils.startOfMonth(offset) / 86400000L
        
        val dailyMap = daily.associate { it.dayBucket to it.total }
        val dailyList = (0 until maxDays).map { day ->
            dailyMap[startDayTimestamp + day] ?: 0.0
        }

        StatsUiState(
            categoryTotals = categoryTotals,
            dailyTotals = dailyList,
            totalExpenses = totalExp,
            totalIncome = totalInc,
            average = totalExp / maxDays,
            currencySymbol = symbol,
            monthsAgo = offset
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StatsUiState())

    fun previousMonth() { monthOffset.value += 1 }
    fun nextMonth() { if (monthOffset.value > 0) monthOffset.value -= 1 }
}
