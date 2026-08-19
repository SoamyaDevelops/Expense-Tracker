package com.expensetracker.app.ui.addedit

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.expensetracker.app.ExpenseApp
import com.expensetracker.app.data.Account
import com.expensetracker.app.data.Category
import com.expensetracker.app.data.Expense
import com.expensetracker.app.data.RecurrenceType
import com.expensetracker.app.data.TransactionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AddEditUiState(
    val id: Long? = null,
    val title: String = "",
    val amountText: String = "",
    val type: TransactionType = TransactionType.EXPENSE,
    val categoryId: Long? = null,
    val accountId: Long? = null,
    val dateMillis: Long = System.currentTimeMillis(),
    val note: String = "",
    val recurrence: RecurrenceType = RecurrenceType.NONE,
    val isAutoPay: Boolean = false,
    val categories: List<Category> = emptyList(),
    val accounts: List<Account> = emptyList(),
    val currencySymbol: String = "$",
    val saved: Boolean = false,
    val error: String? = null
)

class AddEditViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = (application as ExpenseApp).repository
    private val settings = (application as ExpenseApp).settingsRepository

    private val _state = MutableStateFlow(AddEditUiState())
    val state: StateFlow<AddEditUiState> = _state

    init {
        viewModelScope.launch {
            repo.getAllCategories().collect { cats ->
                _state.value = _state.value.copy(
                    categories = cats,
                    categoryId = _state.value.categoryId ?: cats.firstOrNull()?.id
                )
            }
        }
        viewModelScope.launch {
            repo.getAllAccounts().collect { accs ->
                _state.value = _state.value.copy(
                    accounts = accs,
                    accountId = _state.value.accountId ?: accs.firstOrNull()?.id
                )
            }
        }
        viewModelScope.launch {
            settings.currencySymbol.collect { symbol ->
                _state.value = _state.value.copy(currencySymbol = symbol)
            }
        }
    }

    fun loadExpense(id: Long) {
        viewModelScope.launch {
            val expense = repo.getExpenseById(id) ?: return@launch
            _state.value = _state.value.copy(
                id = expense.id,
                title = expense.title,
                amountText = if (expense.amount % 1.0 == 0.0) expense.amount.toLong().toString() else expense.amount.toString(),
                type = expense.type,
                categoryId = expense.categoryId,
                accountId = expense.accountId,
                dateMillis = expense.dateMillis,
                note = expense.note,
                recurrence = expense.recurrence,
                isAutoPay = expense.isAutoPay
            )
        }
    }

    fun updateTitle(value: String) { _state.value = _state.value.copy(title = value, error = null) }
    fun updateAmount(value: String) {
        val filtered = value.filter { it.isDigit() || it == '.' }
        _state.value = _state.value.copy(amountText = filtered, error = null)
    }
    fun updateType(value: TransactionType) { _state.value = _state.value.copy(type = value) }
    fun updateCategory(id: Long) { _state.value = _state.value.copy(categoryId = id) }
    fun updateAccount(id: Long) { _state.value = _state.value.copy(accountId = id) }
    fun updateDate(millis: Long) { _state.value = _state.value.copy(dateMillis = millis) }
    fun updateNote(value: String) { _state.value = _state.value.copy(note = value) }
    fun updateRecurrence(value: RecurrenceType) { _state.value = _state.value.copy(recurrence = value) }
    fun updateAutoPay(value: Boolean) { _state.value = _state.value.copy(isAutoPay = value) }

    fun save() {
        val s = _state.value
        val amount = s.amountText.toDoubleOrNull()
        if (s.title.isBlank()) {
            _state.value = s.copy(error = "Please add a title")
            return
        }
        if (amount == null || amount <= 0.0) {
            _state.value = s.copy(error = "Enter a valid amount")
            return
        }
        viewModelScope.launch {
            val expense = Expense(
                id = s.id ?: 0,
                title = s.title.trim(),
                amount = amount,
                type = s.type,
                categoryId = s.categoryId,
                accountId = s.accountId,
                dateMillis = s.dateMillis,
                note = s.note.trim(),
                recurrence = s.recurrence,
                isAutoPay = s.isAutoPay
            )
            if (s.id == null) repo.addExpense(expense) else repo.updateExpense(expense)
            _state.value = _state.value.copy(saved = true)
        }
    }
}
