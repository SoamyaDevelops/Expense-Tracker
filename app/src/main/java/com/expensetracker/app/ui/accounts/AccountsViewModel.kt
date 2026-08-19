package com.expensetracker.app.ui.accounts

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.expensetracker.app.ExpenseApp
import com.expensetracker.app.data.Account
import com.expensetracker.app.data.AccountType
import com.expensetracker.app.data.TransactionType
import com.expensetracker.app.data.Transfer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class AccountsUiState(
    val accounts: List<Account> = emptyList(),
    val transfers: List<Transfer> = emptyList(),
    val currencySymbol: String = "$"
)

class AccountsViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = (application as ExpenseApp).repository
    private val settings = (application as ExpenseApp).settingsRepository

    private val _uiState = MutableStateFlow(AccountsUiState())
    val uiState: StateFlow<AccountsUiState> = _uiState

    init {
        viewModelScope.launch {
            repo.getAllAccounts().collectLatest { accs ->
                _uiState.value = _uiState.value.copy(accounts = accs)
            }
        }
        viewModelScope.launch {
            repo.getAllTransfers().collectLatest { transfers ->
                _uiState.value = _uiState.value.copy(transfers = transfers)
            }
        }
        viewModelScope.launch {
            settings.currencySymbol.collect { symbol ->
                _uiState.value = _uiState.value.copy(currencySymbol = symbol)
            }
        }
    }

    fun addAccount(name: String, balance: Double, type: AccountType) {
        viewModelScope.launch {
            repo.addAccount(Account(name = name, balance = balance, type = type))
        }
    }

    fun updateAccount(account: Account) {
        viewModelScope.launch {
            repo.updateAccount(account)
        }
    }

    fun deleteAccount(account: Account) {
        viewModelScope.launch {
            repo.deleteAccount(account)
        }
    }

    fun adjustBalance(account: Account, newBalance: Double) {
        viewModelScope.launch {
            val diff = newBalance - account.balance
            if (diff == 0.0) return@launch
            
            val type = if (diff > 0) TransactionType.INCOME else TransactionType.EXPENSE
            val amount = Math.abs(diff)
            
            repo.addExpense(
                com.expensetracker.app.data.Expense(
                    title = "Balance Adjustment",
                    amount = amount,
                    type = type,
                    categoryId = null,
                    accountId = account.id,
                    dateMillis = System.currentTimeMillis(),
                    note = "Manual adjustment to match actual balance"
                )
            )
        }
    }

    fun transfer(fromAccountId: Long, toAccountId: Long, amount: Double) {
        viewModelScope.launch {
            repo.addTransfer(
                Transfer(
                    fromAccountId = fromAccountId,
                    toAccountId = toAccountId,
                    amount = amount,
                    dateMillis = System.currentTimeMillis()
                )
            )
        }
    }
}
