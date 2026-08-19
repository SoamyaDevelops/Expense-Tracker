package com.expensetracker.app.ui.debts

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.expensetracker.app.ExpenseApp
import com.expensetracker.app.data.Debt
import com.expensetracker.app.data.DebtType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DebtUiState(
    val debts: List<Debt> = emptyList(),
    val currencySymbol: String = "$",
    val totalLent: Double = 0.0,
    val totalBorrowed: Double = 0.0
)

class DebtViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = (application as ExpenseApp).repository
    private val settings = (application as ExpenseApp).settingsRepository

    val uiState: StateFlow<DebtUiState> = combine(
        repo.getAllDebts(),
        settings.currencySymbol
    ) { debts, symbol ->
        DebtUiState(
            debts = debts,
            currencySymbol = symbol,
            totalLent = debts.filter { it.type == DebtType.LENT && !it.isResolved }.sumOf { it.amount },
            totalBorrowed = debts.filter { it.type == DebtType.BORROWED && !it.isResolved }.sumOf { it.amount }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DebtUiState())

    fun addDebt(personName: String, amount: Double, type: DebtType, note: String) {
        viewModelScope.launch {
            repo.addDebt(Debt(personName = personName, amount = amount, type = type, note = note))
        }
    }

    fun toggleResolved(debt: Debt) {
        viewModelScope.launch {
            repo.updateDebt(debt.copy(isResolved = !debt.isResolved))
        }
    }

    fun deleteDebt(debt: Debt) {
        viewModelScope.launch {
            repo.deleteDebt(debt)
        }
    }
}
