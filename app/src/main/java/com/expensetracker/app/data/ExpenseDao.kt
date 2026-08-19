package com.expensetracker.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

data class CategoryTotal(val categoryId: Long?, val total: Double)
data class DayTotal(val dayBucket: Long, val total: Double)

@Dao
interface ExpenseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(expense: Expense): Long

    @Update
    suspend fun update(expense: Expense)

    @Delete
    suspend fun delete(expense: Expense)

    @Query("SELECT * FROM expenses WHERE id = :id")
    suspend fun getById(id: Long): Expense?

    @Query("SELECT * FROM expenses ORDER BY id DESC")
    fun getAll(): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE dateMillis BETWEEN :start AND :end ORDER BY id DESC")
    fun getBetween(start: Long, end: Long): Flow<List<Expense>>

    @Query("""
        SELECT * FROM expenses
        WHERE (:query = '' OR title LIKE '%' || :query || '%' OR note LIKE '%' || :query || '%')
        AND (:categoryId IS NULL OR categoryId = :categoryId)
        ORDER BY id DESC
    """)
    fun search(query: String, categoryId: Long?): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE isFavorite = 1 ORDER BY id DESC")
    fun getFavorites(): Flow<List<Expense>>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM expenses WHERE type = 'EXPENSE' AND dateMillis BETWEEN :start AND :end")
    fun getTotalExpensesBetween(start: Long, end: Long): Flow<Double>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM expenses WHERE type = 'INCOME' AND dateMillis BETWEEN :start AND :end")
    fun getTotalIncomeBetween(start: Long, end: Long): Flow<Double>

    @Query("SELECT categoryId, COALESCE(SUM(amount),0) as total FROM expenses WHERE type = 'EXPENSE' AND dateMillis BETWEEN :start AND :end GROUP BY categoryId")
    fun getTotalsByCategory(start: Long, end: Long): Flow<List<CategoryTotal>>

    @Query("SELECT (dateMillis / 86400000) as dayBucket, COALESCE(SUM(amount),0) as total FROM expenses WHERE type = 'EXPENSE' AND dateMillis BETWEEN :start AND :end GROUP BY dayBucket ORDER BY dayBucket ASC")
    fun getDailyTotals(start: Long, end: Long): Flow<List<DayTotal>>

    @Query("SELECT COUNT(*) FROM expenses")
    fun getCount(): Flow<Int>

    @Query("SELECT COALESCE(MAX(amount),0) FROM expenses WHERE type = 'EXPENSE' AND dateMillis BETWEEN :start AND :end")
    fun getMaxBetween(start: Long, end: Long): Flow<Double>
}
