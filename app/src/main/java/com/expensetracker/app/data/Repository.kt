package com.expensetracker.app.data

import androidx.room.Transaction
import com.expensetracker.app.data.Debt
import com.expensetracker.app.data.DebtType
import kotlinx.coroutines.flow.Flow

class ExpenseRepository(
    private val expenseDao: ExpenseDao,
    private val categoryDao: CategoryDao,
    private val accountDao: AccountDao,
    private val transferDao: TransferDao,
    private val debtDao: DebtDao
) {
    // Expenses
    fun getAllExpenses(): Flow<List<Expense>> = expenseDao.getAll()
    fun getExpensesBetween(start: Long, end: Long): Flow<List<Expense>> = expenseDao.getBetween(start, end)
    fun search(query: String, categoryId: Long?): Flow<List<Expense>> = expenseDao.search(query, categoryId)
    fun getFavorites(): Flow<List<Expense>> = expenseDao.getFavorites()
    fun getTotalExpensesBetween(start: Long, end: Long): Flow<Double> = expenseDao.getTotalExpensesBetween(start, end)
    fun getTotalIncomeBetween(start: Long, end: Long): Flow<Double> = expenseDao.getTotalIncomeBetween(start, end)
    fun getTotalsByCategory(start: Long, end: Long): Flow<List<CategoryTotal>> = expenseDao.getTotalsByCategory(start, end)
    fun getDailyTotals(start: Long, end: Long): Flow<List<DayTotal>> = expenseDao.getDailyTotals(start, end)
    fun getCount(): Flow<Int> = expenseDao.getCount()

    suspend fun addExpense(expense: Expense): Long {
        val id = expenseDao.insert(expense)
        val category = expense.categoryId?.let { categoryDao.getById(it) }
        
        // Account balance sync
        expense.accountId?.let { accountId ->
            accountDao.getById(accountId)?.let { account ->
                val newBalance = if (expense.type == TransactionType.INCOME) {
                    account.balance + expense.amount
                } else {
                    account.balance - expense.amount
                }
                accountDao.update(account.copy(balance = newBalance))
            }
        }

        // Debt/Lend sync
        syncDebtOnAdd(expense.copy(id = id), category)
        
        return id
    }

    private suspend fun syncDebtOnAdd(expense: Expense, category: Category?) {
        when (category?.name) {
            "Debt" -> {
                if (expense.type == TransactionType.INCOME) {
                    // Borrowing money
                    debtDao.insert(Debt(
                        personName = expense.title,
                        amount = expense.amount,
                        type = DebtType.BORROWED,
                        dateMillis = expense.dateMillis,
                        note = expense.note,
                        linkedExpenseId = expense.id
                    ))
                } else {
                    // Clearing debt
                    debtDao.findUnresolved(expense.title, DebtType.BORROWED)?.let {
                        debtDao.update(it.copy(isResolved = true))
                    }
                }
            }
            "Lend" -> {
                if (expense.type == TransactionType.EXPENSE) {
                    // Lending money
                    debtDao.insert(Debt(
                        personName = expense.title,
                        amount = expense.amount,
                        type = DebtType.LENT,
                        dateMillis = expense.dateMillis,
                        note = expense.note,
                        linkedExpenseId = expense.id
                    ))
                } else {
                    // Recovering money
                    debtDao.findUnresolved(expense.title, DebtType.LENT)?.let {
                        debtDao.update(it.copy(isResolved = true))
                    }
                }
            }
        }
    }

    suspend fun updateExpense(expense: Expense) {
        val oldExpense = expenseDao.getById(expense.id) ?: return
        expenseDao.update(expense)
        
        // Revert old account balance
        oldExpense.accountId?.let { accountId ->
            accountDao.getById(accountId)?.let { account ->
                val revertedBalance = if (oldExpense.type == TransactionType.INCOME) {
                    account.balance - oldExpense.amount
                } else {
                    account.balance + oldExpense.amount
                }
                accountDao.update(account.copy(balance = revertedBalance))
            }
        }
        
        // Apply new account balance
        expense.accountId?.let { accountId ->
            accountDao.getById(accountId)?.let { account ->
                val newBalance = if (expense.type == TransactionType.INCOME) {
                    account.balance + expense.amount
                } else {
                    account.balance - expense.amount
                }
                accountDao.update(account.copy(balance = newBalance))
            }
        }

        // Debt sync: simplest way is delete old linked debt and create new one if applicable
        debtDao.getByExpenseId(expense.id).forEach { debtDao.delete(it) }
        val category = expense.categoryId?.let { categoryDao.getById(it) }
        syncDebtOnAdd(expense, category)
    }

    suspend fun deleteExpense(expense: Expense) {
        expenseDao.delete(expense)
        
        // Account balance sync
        expense.accountId?.let { accountId ->
            accountDao.getById(accountId)?.let { account ->
                val newBalance = if (expense.type == TransactionType.INCOME) {
                    account.balance - expense.amount
                } else {
                    account.balance + expense.amount
                }
                accountDao.update(account.copy(balance = newBalance))
            }
        }

        // Debt sync
        debtDao.getByExpenseId(expense.id).forEach { debtDao.delete(it) }
    }

    suspend fun getExpenseById(id: Long): Expense? = expenseDao.getById(id)

    // Categories
    fun getAllCategories(): Flow<List<Category>> = categoryDao.getAll()
    suspend fun addCategory(category: Category): Long = categoryDao.insert(category)
    suspend fun updateCategory(category: Category) = categoryDao.update(category)
    suspend fun deleteCategory(category: Category) = categoryDao.delete(category)
    suspend fun getCategoryById(id: Long): Category? = categoryDao.getById(id)
    suspend fun getCategoryByName(name: String): Category? = categoryDao.getByName(name)

    // Accounts
    fun getAllAccounts(): Flow<List<Account>> = accountDao.getAll()
    suspend fun getAccountById(id: Long): Account? = accountDao.getById(id)
    suspend fun getAccountByName(name: String): Account? = accountDao.getByName(name)
    suspend fun addAccount(account: Account): Long = accountDao.insert(account)
    suspend fun updateAccount(account: Account) = accountDao.update(account)
    suspend fun deleteAccount(account: Account) = accountDao.delete(account)

    // Transfers
    fun getAllTransfers(): Flow<List<Transfer>> = transferDao.getAll()
    suspend fun addTransfer(transfer: Transfer) {
        transferDao.insert(transfer)
        // Update from account
        accountDao.getById(transfer.fromAccountId)?.let { fromAcc ->
            accountDao.update(fromAcc.copy(balance = fromAcc.balance - transfer.amount))
        }
        // Update to account
        accountDao.getById(transfer.toAccountId)?.let { toAcc ->
            accountDao.update(toAcc.copy(balance = toAcc.balance + transfer.amount))
        }
    }

    // Debts
    fun getAllDebts(): Flow<List<Debt>> = debtDao.getAll()
    suspend fun addDebt(debt: Debt): Long = debtDao.insert(debt)
    suspend fun updateDebt(debt: Debt) = debtDao.update(debt)
    suspend fun deleteDebt(debt: Debt) = debtDao.delete(debt)
}
