package com.example.data

import kotlinx.coroutines.flow.Flow

class RoutineRepository(private val dailyRoutineDao: DailyRoutineDao) {

    fun getRoutineState(date: String): Flow<DailyRoutineState?> {
        return dailyRoutineDao.getRoutineState(date)
    }

    suspend fun getOrCreateRoutineStateDirect(date: String): DailyRoutineState {
        val existing = dailyRoutineDao.getRoutineStateDirect(date)
        if (existing == null) {
            val newState = DailyRoutineState(date = date)
            dailyRoutineDao.insertOrUpdate(newState)
            return newState
        }
        return existing
    }

    suspend fun updateBrush(date: String, completed: Boolean) {
        val current = getOrCreateRoutineStateDirect(date)
        dailyRoutineDao.insertOrUpdate(
            current.copy(brush = completed, lastUpdated = System.currentTimeMillis())
        )
    }

    suspend fun updateBath(date: String, completed: Boolean) {
        val current = getOrCreateRoutineStateDirect(date)
        dailyRoutineDao.insertOrUpdate(
            current.copy(bath = completed, lastUpdated = System.currentTimeMillis())
        )
    }

    suspend fun updateTradingTime(date: String, completed: Boolean) {
        val current = getOrCreateRoutineStateDirect(date)
        dailyRoutineDao.insertOrUpdate(
            current.copy(tradingTime = completed, lastUpdated = System.currentTimeMillis())
        )
    }

    suspend fun updateTradingLiquidity(date: String, completed: Boolean) {
        val current = getOrCreateRoutineStateDirect(date)
        dailyRoutineDao.insertOrUpdate(
            current.copy(tradingLiquidity = completed, lastUpdated = System.currentTimeMillis())
        )
    }

    suspend fun updateTradingDisplacement(date: String, completed: Boolean) {
        val current = getOrCreateRoutineStateDirect(date)
        dailyRoutineDao.insertOrUpdate(
            current.copy(tradingDisplacement = completed, lastUpdated = System.currentTimeMillis())
        )
    }

    suspend fun updatePuzzleSolved(date: String, completed: Boolean) {
        val current = getOrCreateRoutineStateDirect(date)
        dailyRoutineDao.insertOrUpdate(
            current.copy(puzzleSolved = completed, lastUpdated = System.currentTimeMillis())
        )
    }

    fun getAllHistory(): Flow<List<DailyRoutineState>> {
        return dailyRoutineDao.getAllHistory()
    }
}
