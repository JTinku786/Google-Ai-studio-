package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.random.Random

@OptIn(ExperimentalCoroutinesApi::class)
class RoutineViewModel(private val repository: RoutineRepository) : ViewModel() {

    private val _currentDate = MutableStateFlow(getTodayDateKey())
    val currentDate: StateFlow<String> = _currentDate.asStateFlow()

    // Observe today's (or selected date's) routine database state
    val routineState: StateFlow<DailyRoutineState> = _currentDate
        .flatMapLatest { date ->
            repository.getRoutineState(date).map { it ?: DailyRoutineState(date = date) }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DailyRoutineState(date = getTodayDateKey())
        )

    // Manual overrides for quote and verse, so users can shuffle them
    private val _shuffledQuote = MutableStateFlow<MotivationalQuote?>(null)
    val currentQuote: StateFlow<MotivationalQuote> = combine(_currentDate, _shuffledQuote) { date, shuffled ->
        shuffled ?: getStableDailyQuote(date)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = getStableDailyQuote(getTodayDateKey())
    )

    private val _shuffledVerse = MutableStateFlow<GitaVerse?>(null)
    val currentVerse: StateFlow<GitaVerse> = combine(_currentDate, _shuffledVerse) { date, shuffled ->
        shuffled ?: getStableDailyVerse(date)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = getStableDailyVerse(getTodayDateKey())
    )

    // Observe historical data to calculate streaks and completion stats
    val history: StateFlow<List<DailyRoutineState>> = repository.getAllHistory()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        // Pre-create the today's record in Room, ensuring initial load resets or fetches exist.
        viewModelScope.launch {
            repository.getOrCreateRoutineStateDirect(getTodayDateKey())
        }
    }

    // --- Actions ---

    fun toggleBrush(completed: Boolean) {
        viewModelScope.launch {
            repository.updateBrush(_currentDate.value, completed)
        }
    }

    fun toggleBath(completed: Boolean) {
        viewModelScope.launch {
            repository.updateBath(_currentDate.value, completed)
        }
    }

    fun toggleTradingTime(completed: Boolean) {
        viewModelScope.launch {
            repository.updateTradingTime(_currentDate.value, completed)
        }
    }

    fun toggleTradingLiquidity(completed: Boolean) {
        viewModelScope.launch {
            repository.updateTradingLiquidity(_currentDate.value, completed)
        }
    }

    fun toggleTradingDisplacement(completed: Boolean) {
        viewModelScope.launch {
            repository.updateTradingDisplacement(_currentDate.value, completed)
        }
    }

    fun solvePuzzle(completed: Boolean) {
        viewModelScope.launch {
            repository.updatePuzzleSolved(_currentDate.value, completed)
        }
    }

    fun shuffleQuote() {
        val nextQuote = DailyRoutineData.quotes.random()
        _shuffledQuote.value = nextQuote
    }

    fun shuffleVerse() {
        val nextVerse = DailyRoutineData.gitaVerses.random()
        _shuffledVerse.value = nextVerse
    }

    fun resetOverriddenQuoteAndVerse() {
        _shuffledQuote.value = null
        _shuffledVerse.value = null
    }

    // --- Date Helpers ---

    fun getTodayDateKey(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(Calendar.getInstance().time)
    }

    fun getFormattedDisplayDate(): String {
        val sdf = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.US)
        return sdf.format(Calendar.getInstance().time)
    }

    // --- Seeded stable daily quote / verse algorithms ---

    private fun getStableDailyQuote(dateKey: String): MotivationalQuote {
        val seed = dateKey.hashCode().toLong()
        val index = Random(seed).nextInt(DailyRoutineData.quotes.size)
        return DailyRoutineData.quotes[index]
    }

    private fun getStableDailyVerse(dateKey: String): GitaVerse {
        val seed = dateKey.hashCode().toLong()
        val index = Random(seed).nextInt(DailyRoutineData.gitaVerses.size)
        return DailyRoutineData.gitaVerses[index]
    }
}
