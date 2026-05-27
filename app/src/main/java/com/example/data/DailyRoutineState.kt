package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_routines")
data class DailyRoutineState(
    @PrimaryKey
    val date: String, // Format: YYYY-MM-DD
    val brush: Boolean = false,
    val bath: Boolean = false,
    val tradingTime: Boolean = false,
    val tradingLiquidity: Boolean = false,
    val tradingDisplacement: Boolean = false,
    val puzzleSolved: Boolean = false,
    val lastUpdated: Long = System.currentTimeMillis()
)
