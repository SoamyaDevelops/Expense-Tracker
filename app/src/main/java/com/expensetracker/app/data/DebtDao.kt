package com.expensetracker.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DebtDao {
    @Query("SELECT * FROM debts ORDER BY dateMillis DESC")
    fun getAll(): Flow<List<Debt>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(debt: Debt): Long

    @Update
    suspend fun update(debt: Debt)

    @Delete
    suspend fun delete(debt: Debt)
    
    @Query("SELECT * FROM debts WHERE id = :id")
    suspend fun getById(id: Long): Debt?

    @Query("SELECT * FROM debts WHERE personName = :name AND type = :type AND isResolved = 0 LIMIT 1")
    suspend fun findUnresolved(name: String, type: DebtType): Debt?

    @Query("SELECT * FROM debts WHERE linkedExpenseId = :expenseId")
    suspend fun getByExpenseId(expenseId: Long): List<Debt>
}
