package com.expensetracker.app.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.expensetracker.app.ExpenseApp
import com.expensetracker.app.data.Category
import com.expensetracker.app.data.Expense
import com.expensetracker.app.util.DateUtils
import com.expensetracker.app.data.TransactionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class RangeFilter { TODAY, WEEK, MONTH, ALL }

data class HomeUiState(
    val expenses: List<Expense> = emptyList(),
    val categories: List<Category> = emptyList(),
    val totalExpenses: Double = 0.0,
    val totalIncome: Double = 0.0,
    val totalBalance: Double = 0.0,
    val budget: Double = 0.0,
    val currencySymbol: String = "$",
    val range: RangeFilter = RangeFilter.MONTH,
    val searchQuery: String = "",
    val filterCategoryId: Long? = null,
    val selectedType: TransactionType? = null,
    val streakDays: Int = 0
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = (application as ExpenseApp).repository
    private val settings = (application as ExpenseApp).settingsRepository

    private val rangeFlow = MutableStateFlow(RangeFilter.MONTH)
    private val searchFlow = MutableStateFlow("")
    private val filterCategoryFlow = MutableStateFlow<Long?>(null)
    private val selectedTypeFlow = MutableStateFlow<TransactionType?>(null)

    private fun rangeBounds(filter: RangeFilter): Pair<Long, Long> = when (filter) {
        RangeFilter.TODAY -> DateUtils.startOfDay() to DateUtils.endOfDay()
        RangeFilter.WEEK -> DateUtils.startOfWeek() to DateUtils.endOfDay()
        RangeFilter.MONTH -> DateUtils.startOfMonth() to DateUtils.endOfMonth()
        RangeFilter.ALL -> 0L to Long.MAX_VALUE
    }

    private val expensesFlow = combine(rangeFlow, searchFlow, filterCategoryFlow, selectedTypeFlow) { range, query, catId, type ->
        val flow = if (query.isNotEmpty() || catId != null) {
            repo.search(query, catId)
        } else {
            val (start, end) = rangeBounds(range)
            repo.getExpensesBetween(start, end)
        }
        flow.map { list ->
            if (type == null) list else list.filter { it.type == type }
        }
    }.flatMapLatest { it }

    private val totalExpensesForRange = rangeFlow.flatMapLatest { range ->
        val (start, end) = rangeBounds(range)
        repo.getTotalExpensesBetween(start, end)
    }

    private val totalIncomeForRange = rangeFlow.flatMapLatest { range ->
        val (start, end) = rangeBounds(range)
        repo.getTotalIncomeBetween(start, end)
    }

    val uiState: StateFlow<HomeUiState> = combine(
        expensesFlow,
        totalExpensesForRange,
        totalIncomeForRange,
        repo.getAllCategories(),
        settings.currencySymbol,
        settings.monthlyBudgetTotal,
        rangeFlow,
        repo.getAllAccounts(),
        searchFlow,
        filterCategoryFlow,
        selectedTypeFlow
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        val expenses = values[0] as List<Expense>
        val totalExp = values[1] as Double
        val totalInc = values[2] as Double
        val categories = values[3] as List<Category>
        val symbol = values[4] as String
        val budget = values[5] as Double
        val range = values[6] as RangeFilter
        val accounts = values[7] as List<com.expensetracker.app.data.Account>
        val search = values[8] as String
        val filterCat = values[9] as Long?
        val type = values[10] as TransactionType?
        
        HomeUiState(
            expenses = expenses,
            categories = categories,
            totalExpenses = totalExp,
            totalIncome = totalInc,
            totalBalance = accounts.sumOf { it.balance },
            budget = budget,
            currencySymbol = symbol,
            range = range,
            searchQuery = search,
            filterCategoryId = filterCat,
            selectedType = type,
            streakDays = computeStreak(expenses)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())

    private fun computeStreak(expenses: List<Expense>): Int {
        if (expenses.isEmpty()) return 0
        val days = expenses.map { DateUtils.formatDate(it.dateMillis, "yyyyMMdd") }.toSet()
        var streak = 0
        var cursor = System.currentTimeMillis()
        while (days.contains(DateUtils.formatDate(cursor, "yyyyMMdd"))) {
            streak++
            cursor -= 86400000L
        }
        return streak
    }

    fun setRange(range: RangeFilter) {
        rangeFlow.value = range
        searchFlow.value = ""
        filterCategoryFlow.value = null
    }

    fun setSearchQuery(query: String) {
        searchFlow.value = query
    }

    fun setFilterCategory(categoryId: Long?) {
        filterCategoryFlow.value = categoryId
    }

    fun setTransactionType(type: TransactionType?) {
        selectedTypeFlow.value = type
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch { repo.deleteExpense(expense) }
    }

    fun toggleFavorite(expense: Expense) {
        viewModelScope.launch { repo.updateExpense(expense.copy(isFavorite = !expense.isFavorite)) }
    }
}
