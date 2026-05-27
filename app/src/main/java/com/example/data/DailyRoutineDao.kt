package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyRoutineDao {
    @Query("SELECT * FROM daily_routines WHERE date = :date")
    fun getRoutineState(date: String): Flow<DailyRoutineState?>

    @Query("SELECT * FROM daily_routines WHERE date = :date")
    suspend fun getRoutineStateDirect(date: String): DailyRoutineState?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(state: DailyRoutineState)

    @Query("SELECT * FROM daily_routines ORDER BY date DESC")
    fun getAllHistory(): Flow<List<DailyRoutineState>>
}
