package com.expensetracker.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TransferDao {
    @Query("SELECT * FROM transfers ORDER BY dateMillis DESC")
    fun getAll(): Flow<List<Transfer>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transfer: Transfer): Long
}
