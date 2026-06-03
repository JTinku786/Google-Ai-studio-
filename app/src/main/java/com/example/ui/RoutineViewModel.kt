package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.data.llm.native.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

data class CoachMode(
    val id: String,
    val name: String,
    val description: String,
    val systemPrompt: String
)

@OptIn(ExperimentalCoroutinesApi::class)
class RoutineViewModel(private val repository: RoutineRepository) : ViewModel() {

    private val _currentDate = MutableStateFlow(getTodayDateKey())
    val currentDate: StateFlow<String> = _currentDate.asStateFlow()

    // --- User Profile ---
    val userProfile: StateFlow<UserProfile> = repository.getUserProfileFlow()
        .map { it ?: repository.getOrCreateUserProfileDirect() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserProfile()
        )

    // --- Legacy / Routine Checklist & Water State ---
    val routineState: StateFlow<DailyRoutineState> = _currentDate
        .flatMapLatest { date ->
            repository.getRoutineState(date).map { it ?: DailyRoutineState(date = date) }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DailyRoutineState(date = getTodayDateKey())
        )

    // --- Active Custom Habits ---
    val activeHabits: StateFlow<List<Habit>> = repository.getActiveHabits()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // --- Habit logs for current date ---
    val habitLogs: StateFlow<List<HabitLog>> = _currentDate
        .flatMapLatest { date -> repository.getHabitLogsForDate(date) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // --- Meal Logs ---
    val mealLogs: StateFlow<List<MealLog>> = _currentDate
        .flatMapLatest { date -> repository.getMealsForDate(date) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // --- Journal Entries for date ---
    val journalEntries: StateFlow<List<JournalEntry>> = _currentDate
        .flatMapLatest { date -> repository.getJournalEntriesForDate(date) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // --- Voice Notes ---
    val voiceNotes: StateFlow<List<VoiceNote>> = repository.getAllVoiceNotes()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // --- Contact Reminders ---
    val contactReminders: StateFlow<List<ContactReminder>> = repository.getAllContactReminders()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // --- Onboarded State ---
    val isOnboarded: StateFlow<Boolean> = userProfile
        .map { it.isOnboarded }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    // --- Daily Mission for currentDate ---
    val dailyMission: StateFlow<DailyMission> = _currentDate
        .flatMapLatest { date ->
            repository.getDailyMission(date).map { it ?: repository.getOrCreateDailyMissionDirect(date, userProfile.value) }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DailyMission(date = getTodayDateKey())
        )

    // --- Night Audit for currentDate ---
    val nightAudit: StateFlow<NightAudit> = _currentDate
        .flatMapLatest { date ->
            repository.getNightAudit(date).map { it ?: repository.getOrCreateNightAuditDirect(date) }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = NightAudit(date = getTodayDateKey())
        )

    // --- Reminder Settings ---
    val reminderSettings: StateFlow<ReminderSettings> = repository.getReminderSettingsFlow()
        .map { it ?: repository.getOrCreateReminderSettings() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ReminderSettings()
        )

    // --- Premium Coach Modes ---
    val coachModes = listOf(
        CoachMode(
            id = "warrior",
            name = "Warrior Discipline",
            description = "Strict elder brother style. Break excuses and win the morning.",
            systemPrompt = "You are a warrior-discipline guru (strict Telugu elder brother). Break lethargy. Urge Tammudu/Chelli to wake up early, tackle hardest tasks first, cold showers, zero excuses, and win the morning by 9 AM. Keep it direct, firm, and supportive."
        ),
        CoachMode(
            id = "food",
            name = "Telugu Food Coach",
            description = "Traditional local wisdom for healthy millet-based eating.",
            systemPrompt = "You are a friendly Telugu nutrition coach. Advise on simple healthy eating: Jowar Roti, Ragi Java, leafy vegetables, clay-pot water, ending food by 8 PM, avoiding deep fries, refined maida, and eating with mindfulness. Comforting local tone."
        ),
        CoachMode(
            id = "gita",
            name = "Gita & Peace",
            description = "Quotes and serene wisdom from the Bhagavad Gita.",
            systemPrompt = "You are a serene spiritual mentor guiding with Gita wisdom. Emphasize Chapter 2, Verse 47: focusing completely on action (Karma) without anxiety over outcomes. Guide the mind out of stress, frustration, and worry into quiet clarity."
        ),
        CoachMode(
            id = "chitkalu",
            name = "Mana Chitkalu",
            description = "Age-old traditional health, home, and focus remedies.",
            systemPrompt = "You are an expert on 'Mana Chitkalu' (traditional Telugu life tips). Teach beneficial daily habits: morning hydration, coconut oil benefits, natural focus builders, breathing rituals, and maintaining organic balance in lifestyle."
        ),
        CoachMode(
            id = "founder",
            name = "Founder Mode",
            description = "High-productivity startup CTO partner for deep focus.",
            systemPrompt = "You are a hyper-focused startup CTO/founder discipline partner. Direct, fast-paced, high intellectual density. Push for Pomodoro deep blocks, zero notifications, shipping code/deals early, and executing daily sprints with strict momentum."
        ),
        CoachMode(
            id = "trader",
            name = "Trader Discipline",
            description = "Emotional balance and strict rule-based market mind.",
            systemPrompt = "You are a strict risk-management and trading coach. Advise on absolute emotional grounding: never overtrade, honor stop-losses, preserve liquid capital, maintain high mental stability, and sleep on time to keep sharpness active."
        ),
        CoachMode(
            id = "fitness",
            name = "Fitness Comeback",
            description = "High-energy coach for healthy movement and physical power.",
            systemPrompt = "You are an energetic, high-octane athletic trainer leading a major fitness comeback. Push for consistent physical exercise, regular hydration, bodyweight circuits, high posture focus, and building a powerful physical temple."
        )
    )

    private val _testAiResult = MutableStateFlow<String>("")
    val testAiResult: StateFlow<String> = _testAiResult.asStateFlow()

    private val _isTestingAi = MutableStateFlow(false)
    val isTestingAi: StateFlow<Boolean> = _isTestingAi.asStateFlow()

    // --- Rich Daily Content (Rotated or AI generated) ---
    val dailyContent: StateFlow<DailyContent> = _currentDate
        .flatMapLatest { date ->
            repository.getDailyContentFlow(date).map { it ?: repository.getOrCreateDailyContentDirect(date) }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DailyRoutineData.getDailyContentForDate(getTodayDateKey())
        )

    // --- AI Companion chat state ---
    private val _chatHistory = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val chatHistory: StateFlow<List<Pair<String, String>>> = _chatHistory.asStateFlow()

    private val _isChatGenerating = MutableStateFlow(false)
    val isChatGenerating: StateFlow<Boolean> = _isChatGenerating.asStateFlow()

    // --- WhatsApp drafted message ---
    private val _whatsAppDraft = MutableStateFlow("")
    val whatsAppDraft: StateFlow<String> = _whatsAppDraft.asStateFlow()

    // --- AI generated plan ---
    private val _aiPlanOutput = MutableStateFlow("")
    val aiPlanOutput: StateFlow<String> = _aiPlanOutput.asStateFlow()

    private val _isPlanGenerating = MutableStateFlow(false)
    val isPlanGenerating: StateFlow<Boolean> = _isPlanGenerating.asStateFlow()

    // Static Quote and Verse getters mapping old properties
    val currentQuote: StateFlow<MotivationalQuote> = dailyContent.map {
        val parts = it.quote.split(" — ")
        MotivationalQuote(1, parts.firstOrNull()?.replace("\"", "") ?: "Stay Disciplined.", parts.getOrNull(1) ?: "Unknown")
    }.stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = MotivationalQuote(1, "Stay disciplined", "Reddy"))

    val currentVerse: StateFlow<GitaVerse> = dailyContent.map {
        GitaVerse(
            id = 1, chapter = 2, verse = 47,
            sanskrit = it.gitaSloka,
            transliteration = "Click below for practical reflection and meaning.",
            translationEnglish = it.gitaMeaningEnglish,
            translationTelugu = it.gitaMeaningTelugu,
            application = "Focus on duty.",
            reflection = "Karmanye vadhikaraste."
        )
    }.stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = DailyRoutineData.gitaVerses[0])

    val llmProvider: LlmProvider = NativeLlamaProvider()

    private val _isModelLoaded = MutableStateFlow(false)
    val isModelLoaded: StateFlow<Boolean> = _isModelLoaded.asStateFlow()

    private val _lastModelLoadStatus = MutableStateFlow<String>("")
    val lastModelLoadStatus: StateFlow<String> = _lastModelLoadStatus.asStateFlow()

    private val _lastGenerationLatency = MutableStateFlow<Long>(0L)
    val lastGenerationLatency: StateFlow<Long> = _lastGenerationLatency.asStateFlow()

    fun loadLocalLlamaModel(
        modelPath: String,
        contextSize: Int = 512,
        threads: Int = 4,
        batchSize: Int = 128,
        temperature: Float = 0.3f,
        maxTokens: Int = 120
    ) {
        viewModelScope.launch {
            _lastModelLoadStatus.value = "Starting on-device GGUF load..."
            val config = LlmConfig(
                contextSize = contextSize,
                maxTokens = maxTokens,
                temperature = temperature,
                threads = threads,
                batchSize = batchSize
            )
            val result = llmProvider.loadModel(modelPath, config)
            _isModelLoaded.value = result.success
            if (result.success) {
                _lastModelLoadStatus.value = "GGUF Model loaded successfully!"
                // Save configurations in UserProfile
                val profile = repository.getOrCreateUserProfileDirect()
                repository.updateUserProfile(profile.copy(
                    selectedGgufPath = modelPath,
                    ggufContextSize = contextSize,
                    ggufMaxTokens = maxTokens,
                    ggufTemperature = temperature,
                    ggufThreads = threads,
                    ggufBatchSize = batchSize
                ))
            } else {
                _lastModelLoadStatus.value = "${result.errorMessage ?: "Failed to map model"}"
            }
        }
    }

    fun unloadLocalLlamaModel() {
        viewModelScope.launch {
            llmProvider.unloadModel()
            _isModelLoaded.value = false
            _lastModelLoadStatus.value = "Model unloaded from memory."
        }
    }

    init {
        viewModelScope.launch {
            // Guarantee profile, initial habits, and initial contact list are seeded
            val profile = repository.getOrCreateUserProfileDirect()
            repository.preseedHabitsIfEmpty()
            repository.preseedContactsIfEmpty()
            repository.getOrCreateRoutineStateDirect(getTodayDateKey())
            repository.getOrCreateDailyContentDirect(getTodayDateKey())
            repository.getOrCreateDailyMissionDirect(getTodayDateKey(), profile)
            repository.getOrCreateNightAuditDirect(getTodayDateKey())
            repository.getOrCreateReminderSettings()

            // Attempt auto load if user left a GGUF model loaded earlier
            if (profile.selectedGgufPath.isNotEmpty()) {
                _lastModelLoadStatus.value = "Resuming persisted GGUF..."
                val config = LlmConfig(
                    contextSize = profile.ggufContextSize,
                    maxTokens = profile.ggufMaxTokens,
                    temperature = profile.ggufTemperature,
                    threads = profile.ggufThreads,
                    batchSize = profile.ggufBatchSize
                )
                val result = llmProvider.loadModel(profile.selectedGgufPath, config)
                _isModelLoaded.value = result.success
                _lastModelLoadStatus.value = if (result.success) "On-device GGUF active." else "Persisted load error: ${result.errorMessage}"
            } else {
                _lastModelLoadStatus.value = "Not loaded. Select a GGUF file in Settings."
            }
        }
    }

    // --- User Actions ---

    fun changeUserProfile(name: String, wake: String, sleep: String, food: String, water: Int) {
        viewModelScope.launch {
            val old = userProfile.value
            repository.updateUserProfile(old.copy(
                name = name, wakeUpTime = wake, sleepTime = sleep,
                foodPreference = food, waterGoalMl = water
            ))
        }
    }

    fun configureOllamaSettings(baseUrl: String, model: String) {
        viewModelScope.launch {
            val old = userProfile.value
            repository.updateUserProfile(old.copy(ollamaBaseUrl = baseUrl, ollamaModel = model))
        }
    }

    // Legacy legacy actions (brush, bath, puzzle)
    fun toggleBrush(completed: Boolean) = viewModelScope.launch { repository.updateBrush(_currentDate.value, completed) }
    fun toggleBath(completed: Boolean) = viewModelScope.launch { repository.updateBath(_currentDate.value, completed) }
    fun toggleTradingTime(completed: Boolean) = viewModelScope.launch { repository.updateTradingTime(_currentDate.value, completed) }
    fun toggleTradingLiquidity(completed: Boolean) = viewModelScope.launch { repository.updateTradingLiquidity(_currentDate.value, completed) }
    fun toggleTradingDisplacement(completed: Boolean) = viewModelScope.launch { repository.updateTradingDisplacement(_currentDate.value, completed) }
    fun solvePuzzle(completed: Boolean) = viewModelScope.launch { repository.updatePuzzleSolved(_currentDate.value, completed) }

    fun addWaterIntake(amountMl: Int) {
        viewModelScope.launch {
            val currentIntake = routineState.value.waterIntakeMl
            val newIntake = (currentIntake + amountMl).coerceAtLeast(0)
            repository.updateWaterIntake(_currentDate.value, newIntake)
        }
    }

    fun shuffleQuote() {
        // Rotates the whole daily contents randomly from local DB fallback to ensure refreshing experience
        viewModelScope.launch {
            val randomKey = "random-date-${Random.nextInt(1000)}"
            val refreshed = DailyRoutineData.getDailyContentForDate(randomKey)
            repository.saveDailyContent(refreshed.copy(date = _currentDate.value))
        }
    }

    fun shuffleVerse() = shuffleQuote()

    // Custom Habit log state actions
    fun toggleHabit(habitId: Long, completed: Boolean) {
        viewModelScope.launch {
            val status = if (completed) "Completed" else "Skipped"
            repository.toggleHabitLog(habitId, _currentDate.value, status, null)
        }
    }

    fun addHabit(name: String, category: String) {
        viewModelScope.launch {
            repository.insertHabit(Habit(name = name, category = category))
        }
    }

    fun removeHabit(id: Long) {
        viewModelScope.launch {
            repository.deleteHabit(id)
        }
    }

    fun toggleHardestTask(completed: Boolean) {
        viewModelScope.launch {
            val dateKey = _currentDate.value
            val profile = userProfile.value
            val m = repository.getOrCreateDailyMissionDirect(dateKey, profile)
            val updated = m.copy(
                hardestTaskCompleted = completed,
                disciplineScore = (m.disciplineScore + if (completed) 25 else -25).coerceIn(0, 100)
            )
            repository.saveDailyMission(updated)
        }
    }

    // Journal entries
    fun saveJournal(type: String, content: String, mood: String? = null, energy: Int? = null) {
        viewModelScope.launch {
            val dateKey = _currentDate.value
            val existing = repository.getJournalEntryDirect(dateKey, type)
            val id = existing?.id ?: 0L
            repository.insertJournalEntry(
                JournalEntry(
                    id = id,
                    date = dateKey,
                    type = type,
                    content = content,
                    mood = mood,
                    energyLevel = energy,
                    aiSummary = existing?.aiSummary
                )
            )
        }
    }

    // Meal Logs
    fun logMeal(mealType: String, description: String, imageUri: String? = null, calories: Int? = null, protein: Int? = null) {
        viewModelScope.launch {
            repository.insertMealLog(
                MealLog(
                    date = _currentDate.value,
                    mealType = mealType,
                    description = description,
                    imageUri = imageUri,
                    estimatedCalories = calories,
                    estimatedProtein = protein
                )
            )
        }
    }

    fun removeMealLog(id: Long) {
        viewModelScope.launch {
            repository.deleteMealLog(id)
        }
    }

    // Add Simulated Voice Note
    fun saveVoiceNote(transcript: String, summary: String? = null, category: String = "Notes") {
        viewModelScope.launch {
            repository.insertVoiceNote(
                VoiceNote(
                    date = _currentDate.value,
                    audioUri = "mock_file_${System.currentTimeMillis()}.amr",
                    transcript = transcript,
                    aiSummary = summary ?: "Discipline log summary of voice journal.",
                    category = category
                )
            )
        }
    }

    fun removeVoiceNote(id: Long) {
        viewModelScope.launch {
            repository.deleteVoiceNote(id)
        }
    }

    // Add Contacts
    fun addNewContact(name: String, phone: String, category: String, frequency: String, notes: String) {
        viewModelScope.launch {
            repository.insertContactReminder(
                ContactReminder(
                    contactName = name, phoneNumber = phone,
                    category = category, reminderFrequency = frequency, notes = notes
                )
            )
        }
    }

    fun removeContact(id: Long) {
        viewModelScope.launch {
            repository.deleteContactReminder(id)
        }
    }

    // Clear Chat
    fun clearChat() {
        _chatHistory.value = emptyList()
    }

    // --- Local AI prompt execution & offline fallback handlers ---

    fun submitChatMessage(text: String, coachRole: String) {
        if (text.trim().isEmpty()) return

        val profile = userProfile.value
        val historyList = _chatHistory.value.toMutableList()
        historyList.add(Pair("user", text))
        _chatHistory.value = historyList

        _isChatGenerating.value = true

        val mode = coachModes.find { it.name == coachRole || it.id == coachRole.lowercase() } ?: coachModes[0]
        val currentM = dailyMission.value
        val currentA = nightAudit.value
        val mealsStr = mealLogs.value.take(2).joinToString { it.description }
        val habitsCount = activeHabits.value.size
        val logsDone = habitLogs.value.count { it.status == "Completed" }
        val donePct = if (habitsCount > 0) (logsDone * 100) / habitsCount else 100

        val chatContext = """
            [USER ON-DEVICE CONTEXT]
            User: ${profile.name} (Type: ${profile.lifeIdentity})
            Routines: Wake: ${profile.wakeUpTime} / Sleep: ${profile.sleepTime}
            Water target: ${profile.waterGoalMl} ml
            7-day Goal: ${profile.mainSevenDayGoal}
            Weakness: ${profile.biggestWeakness}
            Today's Mission Status: ${if (currentM.isGenerated) "Command: " + currentM.aiMorningCommand.take(100) + "..." else "Standard"}
            Habits done today: $donePct% completed
            Recent eating log: $mealsStr
            Current Selected Coach Mode: ${mode.name}

            Rules: Speak with confidence. Address them respectfully. Keep answers under 4 lines.
        """.trimIndent()

        val systemPrompt = """
            ${mode.systemPrompt}

            $chatContext
        """.trimIndent()

        viewModelScope.launch {
            var finalResponse = ""
            if (_isModelLoaded.value) {
                val result = llmProvider.generate(
                    LlmRequest(
                        systemPrompt = systemPrompt,
                        userPrompt = text,
                        maxTokens = profile.ggufMaxTokens,
                        temperature = profile.ggufTemperature
                    )
                )
                if (result.success) {
                    finalResponse = result.text
                    _lastGenerationLatency.value = result.latencyMs
                } else {
                    finalResponse = generateOfflineResponseFallbackForMode(text, mode.id) + "\n\n💡 (Local model load failed: ${result.errorMessage}. Loaded offline fallback.)"
                }
            } else {
                finalResponse = generateOfflineResponseFallbackForMode(text, mode.id) + "\n\n💡 (Model not loaded. Go to Settings/Private Local AI Engine to select a GGUF file. Loaded offline fallback.)"
            }
            val updatedHistory = _chatHistory.value.toMutableList()
            updatedHistory.add(Pair("assistant", finalResponse))
            _chatHistory.value = updatedHistory
            _isChatGenerating.value = false
        }
    }

    fun regenerateLastMessage(coachRole: String) {
        val currentHistory = _chatHistory.value
        if (currentHistory.size < 2) return
        val lastUserMessage = currentHistory.lastOrNull { it.first == "user" }?.second ?: return
        val updatedHistory = currentHistory.toMutableList()
        if (updatedHistory.last().first == "assistant") {
            updatedHistory.removeAt(updatedHistory.size - 1)
        }
        _chatHistory.value = updatedHistory
        submitChatMessage(lastUserMessage, coachRole)
    }

    private fun generateOfflineResponseFallbackForMode(query: String, modeId: String): String {
        val profile = userProfile.value
        return when (modeId) {
            "warrior" -> "Listen Tammudu ${profile.name}, focus is built upon rigid daily routines, not fleeting emotions. Your weakness is listed as '${profile.biggestWeakness}', which means we must eliminate digital scrolling or lazy behaviors immediately. Hydrate with ${profile.waterGoalMl}ml, rise early, and knock out your hardest tasks before seeking comfort."
            "food" -> "Greetings Tammudu! Since we are offline, let's stick to our local traditional wisdom: start your day with warm Ragi Java with crushed almonds. For lunch, prefer healthy millet rice or Jowar Roti with curry. Ensure you close your last meal before 8 PM to give your system ample rest."
            "gita" -> "In times of doubt, refer to Chapter 2, Verse 47 of the Gita: 'Karmanye vadhikaraste ma phaleshu kadachana...' Put your absolute energy into doing your duties properly without anxious anticipation of rewards. Let your work be your worship today."
            "chitkalu" -> "Here is a useful traditional Telugu life tip: Sip warm ginger-water throughout the day to boost digestion, rising early to do 5 minutes of deep breathing before checking any notifications, and store your water in a clay pot for natural mineral coolant."
            "founder" -> "CTO workspace alignment: No side meetings before 12 PM. Block out at least a 2-hour uninterrupted deep sprint on your core business/coding deliverable today. Speed of execution and zero notifications are the key multipliers."
            "trader" -> "Market discipline protocol: Never enter a trade with emotional impulse. Always write down your entry triggers and exit stop-losses. Preserve your capital, avoid overtrading, and keep a cool head. Risk management is everything."
            "fitness" -> "Lethargy ends now! Set a timer for 20 minutes right now. Execute 3 rounds of bodyweight squats, pushups, and high planks. Feed the physical vessel of your life with consistent, active movement!"
            else -> "Hello ${profile.name}! Keep hydrated, log your daily habits, and commit to completing your evening audit reflection to measure results. Small compounding achievements lead to massive transformations."
        }
    }

    fun generateDailyOllamaPlan(goals: String, mood: String, energy: Int) {
        _isPlanGenerating.value = true
        val profile = userProfile.value

        val prompt = """
            Generate an exceptionally disciplined, grounded daily calendar routine.
            User Profile:
            - Name: ${profile.name}
            - Telugu/Telangana values
            - Wakeup: ${profile.wakeUpTime}
            - Sleep: ${profile.sleepTime}
            - Top Goals: $goals
            - Mood today: $mood
            - Energy index: $energy / 10
            
            Synthesize a realistic day layout in bullet format specifying exact hours for hydration, routine checks, deep focus, learning, meal choices, calling a friend/family, and evening recovery.
        """.trimIndent()

        val systemPrompt = "You are a master productivity coach. Craft high-discipline lifestyle plans."

        viewModelScope.launch {
            var fullPlan = ""
            if (_isModelLoaded.value) {
                val result = llmProvider.generate(
                    LlmRequest(
                        systemPrompt = systemPrompt,
                        userPrompt = prompt,
                        maxTokens = profile.ggufMaxTokens,
                        temperature = profile.ggufTemperature
                    )
                )
                if (result.success) {
                    fullPlan = result.text
                    _lastGenerationLatency.value = result.latencyMs
                } else {
                    fullPlan = getOfflinePlanFallback(goals, mood, energy) + "\n\n💡 (Local GGUF engine error: ${result.errorMessage}. Loaded offline fallback.)"
                }
            } else {
                fullPlan = getOfflinePlanFallback(goals, mood, energy) + "\n\n💡 (Local GGUF model is not loaded. Go to Settings to load. Loaded offline fallback.)"
            }
            _aiPlanOutput.value = fullPlan
            saveJournal("Plan", fullPlan, mood, energy)
            _isPlanGenerating.value = false
        }
    }

    fun generateWhatsAppDraft(contact: ContactReminder, purpose: String) {
        val profile = userProfile.value
        val prompt = """
            Generate a short, warm, natural message for ${contact.contactName}.
            Category: ${contact.category}
            Relationship Context: ${contact.notes ?: "Close relationship"}
            Purpose: $purpose
            
            Rules:
            - Write in a natural, warm, friendly tone.
            - Keep it to 1-2 sentences.
            - Do not be overly dramatic or professional.
            - You can include simple Telugu words if fits, like 'Mama' or 'Bro' or warm respects.
        """.trimIndent()

        val systemPrompt = "You are a warm friendship and social relationship helper."

        viewModelScope.launch {
            var draftResult = ""
            if (_isModelLoaded.value) {
                val result = llmProvider.generate(
                    LlmRequest(
                        systemPrompt = systemPrompt,
                        userPrompt = prompt,
                        maxTokens = 80,
                        temperature = 0.3f
                    )
                )
                if (result.success) {
                    draftResult = result.text
                    _lastGenerationLatency.value = result.latencyMs
                } else {
                    draftResult = getOfflineWhatsAppFallback(contact, purpose)
                }
            } else {
                draftResult = getOfflineWhatsAppFallback(contact, purpose)
            }
            _whatsAppDraft.value = draftResult
        }
    }

    // --- Offline Fallback Content Generators ---

    private fun generateOfflineResponseFallback(query: String, role: String): String {
        val q = query.lowercase()
        return when (role) {
            "Discipline Coach", "Morning Coach" -> "Focus is built upon routines, not feelings. I see you asked about acting on habits. My advice: Clear all distractions, put your phone in another room, set a 25-minute timer, and do the single most challenging task first. This builds momentum."
            "Food Coach" -> "Since my local AI server is offline, I suggest sticking to Telangana's healthy grains: A warm bowl of Ragi Java with some soaked almonds in the morning, followed by light Jowar Roti with vegetables at night to maximize digestion and stable energy. Avoid snacking late."
            "Spiritual Coach" -> "In times of uncertainty, refer to chapter 2, verse 47 of the Gita: 'Karmanye vadhikaraste... Do your work with focus, but let go of the results.' Let your priorities be your worship today."
            "Financial Coach" -> "Educational rule of thumb: An emergency fund securing 6 months of absolute expenses must precede any trading or high-risk investments. Invest consistently in high-grade index funds or passive mutual funds."
            "Telugu Chitkalu Coach" -> "ఇక్కడున్న ముఖ్యమైన చిట్కా: ఉదయం నిద్ర లేచిన వెంటనే ఫోన్ చూడకండి, గోరువెచ్చని నీటిని తాగి మీ దినచర్యను ప్రశాంతంగా ప్రారంభించండి."
            else -> "Hello! I am your Pratidinam local coach. While my advanced Gemma neural network is offline, I advise you to stay hydrated, log your daily habits, write down your evening reflections, and prioritize calling one friend or family member today. Small daily strides compile into absolute success."
        }
    }

    private fun getOfflinePlanFallback(goals: String, mood: String, energy: Int): String {
        val profile = userProfile.value
        return """
            📅 **OFFLINE PRATIDINAM LIFE OS PLAN FOR ${profile.name.uppercase()}**
            *Based on your configured times: ${profile.wakeUpTime} to ${profile.sleepTime}*
            
            • **06:00 AM - 06:45 AM | Morning Discipline Block**
              - Drink 500ml water immediately.
              - Cold shower to shock lethargy out of the nervous system.
              - 5 minutes deep breathing and Gita reflection.
            
            • **06:45 AM - 07:15 AM | Mind Clearing**
              - Write morning journal and log 3 primary goals: *$goals*.
              - Mood index: $mood (Energy index: $energy/10).
            
            • **07:30 AM - 08:00 AM | Fuel Step**
              - Protein-rich breakfast (Ragi Java with nuts or Boiled eggs).
            
            • **08:30 AM - 12:30 PM | Deep Focus Work**
              - 4 hours uninterrupted focus. Turn off notifications.
            
            • **01:00 PM - 02:00 PM | Conscious Nutrition**
              - Jowar Roti or Millet rice with Pappu Dal and leafy vegetables.
            
            • **06:00 PM - 07:00 PM | Health Flow**
              - Physical movement, gym, or walking.
            
            • **08:00 PM - 08:30 PM | Light Recovery Meal**
              - Light dinner (chapati + curd or clear soup). Avoid overeating.
            
            • **09:00 PM - 09:30 PM | Connection & Learn Block**
              - Call one family member / close friend as scheduled in Contact Helper.
              - Read one financial topic (e.g. Emergency funds compounding).
            
            • **10:00 PM - 10:30 PM | Evening Wind Down**
              - Write end-of-day reflection journal. Turn screens off! Sleep calmly.
        """.trimIndent()
    }

    private fun getOfflineWhatsAppFallback(contact: ContactReminder, purpose: String): String {
        val name = contact.contactName.replace(Regex("\\(.*\\)"), "").trim()
        val p = purpose.lowercase()
        return when {
            p.contains("morning") || p.contains("greet") -> "Good morning $name, hope you are doing well. Just wanted to connect and wish you a peaceful, productive day ahead!"
            p.contains("check") || p.contains("health") -> "Hi $name, just thinking of you! Hope everything is going great and you are taking care of your health. Let's catch up soon, bro."
            p.contains("thanks") || p.contains("gratitude") -> "Hey $name, wanted to send a quick message to thank you for your support recently. Grateful to have you in my sphere."
            else -> "Hi $name, hope you are doing great. Just checking in on you. Have a productive day!"
        }
    }

    // --- State and Formatting Helpers ---

    private val _isMissionGenerating = MutableStateFlow(false)
    val isMissionGenerating: StateFlow<Boolean> = _isMissionGenerating.asStateFlow()

    private val _isAuditGenerating = MutableStateFlow(false)
    val isAuditGenerating: StateFlow<Boolean> = _isAuditGenerating.asStateFlow()

    fun submitOnboarding(
        name: String,
        lifeIdentity: String,
        wake: String,
        sleep: String,
        water: Int,
        mainGoal: String,
        weakness: String,
        tone: String,
        lang: String
    ) {
        viewModelScope.launch {
            val old = userProfile.value
            val updated = old.copy(
                name = name,
                lifeIdentity = lifeIdentity,
                wakeUpTime = wake,
                sleepTime = sleep,
                waterGoalMl = water,
                mainSevenDayGoal = mainGoal,
                biggestWeakness = weakness,
                preferredCoachTone = tone,
                preferredLanguage = lang,
                isOnboarded = true
            )
            repository.updateUserProfile(updated)
            generateTransformationPlan(updated)
        }
    }

    fun updateOnboardingAnswers(
        lifeIdentity: String,
        mainGoal: String,
        weakness: String,
        tone: String,
        lang: String
    ) {
        viewModelScope.launch {
            val old = userProfile.value
            val updated = old.copy(
                lifeIdentity = lifeIdentity,
                mainSevenDayGoal = mainGoal,
                biggestWeakness = weakness,
                preferredCoachTone = tone,
                preferredLanguage = lang
            )
            repository.updateUserProfile(updated)
        }
    }

    private fun generateTransformationPlan(profile: UserProfile) {
        _isPlanGenerating.value = true
        val prompt = """
            Make a professional, strict 7-Day Lifestyle Transformation Plan for ${profile.name}.
            Identity Context: ${profile.lifeIdentity}
            Wake Up: ${profile.wakeUpTime}
            Sleep/Wind-down: ${profile.sleepTime}
            Weekly Goal: ${profile.mainSevenDayGoal}
            Biggest Weakness: ${profile.biggestWeakness}
            Coach Tone: ${profile.preferredCoachTone}
            Language Mode: ${profile.preferredLanguage}
            
            Synthesize a brief 7-day milestone guide (Day 1-2, Day 3-5, Day 6-7) to optimize their specific lifestyle. Keep it powerful and concise.
        """.trimIndent()

        val systemPrompt = "You are a master life strategist and personal guru. Draft brief but strict lifestyle transformation paths."

        viewModelScope.launch {
            var fullPlan = ""
            if (_isModelLoaded.value) {
                val result = llmProvider.generate(
                    LlmRequest(
                        systemPrompt = systemPrompt,
                        userPrompt = prompt,
                        maxTokens = profile.ggufMaxTokens * 2,
                        temperature = profile.ggufTemperature
                    )
                )
                if (result.success) {
                    fullPlan = result.text
                    _lastGenerationLatency.value = result.latencyMs
                } else {
                    fullPlan = getTransformationPlanFallback(profile)
                }
            } else {
                fullPlan = getTransformationPlanFallback(profile)
            }
            _aiPlanOutput.value = fullPlan
            saveJournal("Plan", fullPlan, "Motivated", 8)
            _isPlanGenerating.value = false
        }
    }

    private fun getTransformationPlanFallback(profile: UserProfile): String {
        return """
            🔥 **YOUR PRIVATE 7-DAY TRANSFORMATION PLAN (${profile.lifeIdentity.uppercase()})**
            *Designed specifically for ${profile.name} to target: ${profile.biggestWeakness}*
            
            • **Days 1 - 2: Foundation & Shock Therapy**
              - Rise exactly at ${profile.wakeUpTime}. Hydrate immediately with 500ml of clean cell-water.
              - Execute the 'Hardest Task First' morning block before opening any social messaging.
              - Begin water goal tracking towards ${profile.waterGoalMl} ml.
            
            • **Days 3 - 5: Consolidating Habits & Friction Removal**
              - Eliminate digital distractions. Place your phone outside of the bedroom before ${profile.sleepTime}.
              - Focus on specific meals: light grains like local Ragi Java for stable focus, light dinings.
              - Touch base with relationships. Execute one scheduled contact reminder call.
            
            • **Days 6 - 7: Reflection & Standardizing Life**
              - Complete daily evening audits to score metrics.
              - Finalize the week with a clear understanding of what caused deviations (e.g., ${profile.biggestWeakness}).
              - Anchor your daily routines in consistent, recurring blocks that require zero willpower.
        """.trimIndent()
    }

    fun generateDailyMission() {
        val profile = userProfile.value
        val dateKey = _currentDate.value
        _isMissionGenerating.value = true

        val prompt = """
            Create today's daily missions for ${profile.name}.
            Identity: ${profile.lifeIdentity}
            Wake time: ${profile.wakeUpTime}
            Sleep wind-down: ${profile.sleepTime}
            Water target: ${profile.waterGoalMl} ml
            7-day Goal: ${profile.mainSevenDayGoal}
            Biggest Weakness: ${profile.biggestWeakness}
            Preferred Tone: ${profile.preferredCoachTone}
            Language: ${profile.preferredLanguage}
            
            Return a short response using active, powerful instruction. Provide:
            - AI Command Message (Direct command based on their weakness and tone)
            - Hardest Task For Today
            - Top 3 Specific Missions for today
            - One Wisdom Quote or Gita verse line
            - One Daily Food suggestion
            - One Family Connection note
            Keep it deeply motivating and high impact!
        """.trimIndent()

        val systemPrompt = "You are a master productivity coach. Craft high-discipline lifestyle guidelines."

        viewModelScope.launch {
            if (_isModelLoaded.value) {
                val result = llmProvider.generate(
                    LlmRequest(
                        systemPrompt = systemPrompt,
                        userPrompt = prompt,
                        maxTokens = profile.ggufMaxTokens * 2,
                        temperature = profile.ggufTemperature
                    )
                )
                if (result.success) {
                    _lastGenerationLatency.value = result.latencyMs
                    val text = result.text
                    val currentM = repository.getOrCreateDailyMissionDirect(dateKey, profile)
                    val updatedM = currentM.copy(
                        aiMorningCommand = text,
                        missionOne = "Focus uninterrupted on your core priority",
                        missionTwo = "Acknowledge and avoid ${profile.biggestWeakness}",
                        missionThree = "Drink ${profile.waterGoalMl} ml water & connect with family",
                        hardestTask = "Tackle biggest priority block in the morning",
                        wisdomQuote = "Focus fully on action, never on anxiety. — Bhagavad Gita",
                        familyReminder = "Remind yourself to call family or check in.",
                        foodSuggestion = "Light traditional nutrition like ragi java or cooked millets.",
                        isGenerated = true
                    )
                    repository.saveDailyMission(updatedM)
                } else {
                    useOfflineMissionFallback(profile, dateKey)
                }
            } else {
                useOfflineMissionFallback(profile, dateKey)
            }
            _isMissionGenerating.value = false
        }
    }

    private suspend fun useOfflineMissionFallback(profile: UserProfile, dateKey: String) {
        val currentM = repository.getOrCreateDailyMissionDirect(dateKey, profile)
        val tone = profile.preferredCoachTone.lowercase()

        val coachCommand = when {
            tone.contains("strict") -> {
                "${profile.name}, today is not for standard excuses. Your biggest weakness is ${profile.biggestWeakness} and that ends today. Rise exactly at ${profile.wakeUpTime}, block all phone scrolling until noon, and drink 500 ml of water immediately. Win this morning."
            }
            tone.contains("spiritual") -> {
                "Perform your actions with total mindfulness, ${profile.name}. Do not worry about long-term results, focus completely on the execution of your Karma today. Maintain peace, stay hydrated, and stay disciplined."
            }
            tone.contains("brother") -> {
                "Listen bro, we need to step up today. Put that phone in another room. Let's hit our ${profile.waterGoalMl}ml water goal and knock out the hardest task by noon. You've got this, no slacking!"
            }
            tone.contains("founder") -> {
                "Founder Mode active. Today's target: complete your high-leverage block first. Zero distractions. Avoid ${profile.biggestWeakness}, consume clean energy, and connect briefly with family. Let's ship results."
            }
            else -> {
                "Focus on small compounding victories today, ${profile.name}. Prioritize hydration, stay calm, and execute your routine steadily."
            }
        }

        val updatedM = currentM.copy(
            aiMorningCommand = coachCommand,
            hardestTask = when {
                profile.biggestWeakness.lowercase().contains("phone") -> "Unplug phone and work 90 minutes uninterrupted"
                profile.biggestWeakness.lowercase().contains("laziness") -> "Do 25 minutes of physical activity and cold shower"
                else -> "Complete your most difficult professional/study/workout task first thing"
            },
            missionOne = "Execute morning work sprint without opening notifications",
            missionTwo = "Track and log meals & water intake carefully",
            missionThree = "Make a meaningful family connection check-in",
            wisdomQuote = "Karmanye vadhikaraste ma phaleshu kadachana. — Bhagavad Gita",
            familyReminder = "Check your Contact Reminders to greet close ones today.",
            foodSuggestion = "Fuel with traditional Ragi Java and raw almonds to maximize focus.",
            isGenerated = true
        )
        repository.saveDailyMission(updatedM)
    }

    fun updateNightAuditMetrics(
        wokeOnTime: Boolean,
        hardestTaskCompleted: Boolean,
        waterConsumedMl: Int,
        foodQuality: String,
        mood: String,
        energyLevel: Int,
        whatWentWell: String,
        whatFailed: String,
        oneThingToImprove: String
    ) {
        viewModelScope.launch {
            val dateKey = _currentDate.value
            val profile = userProfile.value
            _isAuditGenerating.value = true

            val wakePoints = if (wokeOnTime) 15 else 0
            val taskPoints = if (hardestTaskCompleted) 25 else 0
            val waterPoints = if (waterConsumedMl >= profile.waterGoalMl) 15 else ((waterConsumedMl.toFloat() / profile.waterGoalMl.toFloat()) * 15).toInt().coerceIn(0, 15)
            
            val habitsList = activeHabits.value
            val loggedHabits = habitLogs.value
            val totalHabits = habitsList.size
            val completedHabits = loggedHabits.count { it.status == "Completed" }
            val habitRatio = if (totalHabits > 0) completedHabits.toFloat() / totalHabits.toFloat() else 1.0f
            val habitPoints = (habitRatio * 20).toInt().coerceIn(0, 20)

            val foodPoints = when (foodQuality) {
                "Great" -> 10
                "Good" -> 8
                "Okay" -> 4
                else -> 0
            }
            
            val reflectionPoints = 10
            val familyPoints = 5 

            val totalScore = (wakePoints + taskPoints + waterPoints + habitPoints + foodPoints + reflectionPoints + familyPoints).coerceIn(0, 100)

            val prompt = """
                Analyze today's results for ${profile.name}:
                Score achieved: $totalScore/100
                Woke on time: $wokeOnTime
                Hardest task completed: $hardestTaskCompleted
                Water tracker: $waterConsumedMl / ${profile.waterGoalMl} ml
                Food logged: $foodQuality quality
                Energy Level: $energyLevel / 10
                What went well: $whatWentWell
                What failed/distracted: $whatFailed
                One focus for tomorrow: $oneThingToImprove
                
                Generate a strict but loving review and action item correction for tomorrow. Keep it short (3-4 sentences), bold, and in their selected tone of ${profile.preferredCoachTone}.
            """.trimIndent()

            val systemPrompt = "You are a highly analytical local lifestyle supervisor. Review metrics strictly and provide tomorrow's adjustment."

            var reflectionText = ""
            var tomorrowCorrectionText = ""

            if (_isModelLoaded.value) {
                val result = llmProvider.generate(
                    LlmRequest(
                        systemPrompt = systemPrompt,
                        userPrompt = prompt,
                        maxTokens = profile.ggufMaxTokens,
                        temperature = profile.ggufTemperature
                    )
                )
                if (result.success) {
                    _lastGenerationLatency.value = result.latencyMs
                    reflectionText = result.text
                    tomorrowCorrectionText = "Focus priority: $oneThingToImprove. Put phone away before sleeping!"
                } else {
                    reflectionText = getOfflineAuditFeedback(totalScore, profile, whatWentWell, whatFailed)
                    tomorrowCorrectionText = "Correction mandate: avoid $whatFailed tomorrow. Tackle first thing."
                }
            } else {
                reflectionText = getOfflineAuditFeedback(totalScore, profile, whatWentWell, whatFailed)
                tomorrowCorrectionText = "Correction mandate: avoid $whatFailed tomorrow. Tackle first thing."
            }

            val audit = NightAudit(
                date = dateKey,
                wokeUpOnTime = wokeOnTime,
                hardestTaskCompleted = hardestTaskCompleted,
                waterConsumedMl = waterConsumedMl,
                foodQuality = foodQuality,
                mood = mood,
                energyLevel = energyLevel,
                whatWentWell = whatWentWell,
                whatFailed = whatFailed,
                oneThingToImprove = oneThingToImprove,
                aiReflection = reflectionText,
                tomorrowCorrection = tomorrowCorrectionText,
                isCompleted = true
            )
            repository.saveNightAudit(audit)

            val currentMission = repository.getOrCreateDailyMissionDirect(dateKey, profile)
            val updatedMission = currentMission.copy(
                disciplineScore = totalScore,
                waterConsumedMl = waterConsumedMl,
                hardestTaskCompleted = hardestTaskCompleted
            )
            repository.saveDailyMission(updatedMission)

            saveJournal("Reflection", "Score: $totalScore\nReflection: $reflectionText\nTomorrow focus: $tomorrowCorrectionText", mood, energyLevel)

            _isAuditGenerating.value = false
        }
    }

    private fun getOfflineAuditFeedback(score: Int, profile: UserProfile, well: String, failed: String): String {
        return when {
            score >= 85 -> "Outstanding day, ${profile.name}! You scored $score/100. Excellent progress: you woke up on time and knocked out your hardest task, building incredible momentum. Keep this absolute focus compounding."
            score >= 60 -> "Decent effort today, ${profile.name}, scoring $score/100. You showed discipline on routines, but your focus leaked. You need to address the blockers: $failed. Tomorrow, eliminate that single point of failure."
            else -> "A difficult day, ${profile.name}, scoring $score/100. This is a temporary detour, not defeat. If you slipped due to $failed, make sure to put the phone away and do your wake up hydration. Tomorrow is yours."
        }
    }

    fun testLocalAi() {
        if (!_isModelLoaded.value) {
            _testAiResult.value = "Local GGUF engine is not loaded!"
            return
        }
        _isTestingAi.value = true
        _testAiResult.value = "Testing local GGUF..."
        viewModelScope.launch {
            val result = llmProvider.generate(
                LlmRequest(
                    systemPrompt = "You are a simple test validator.",
                    userPrompt = "Reply in one short line: GGUF model is working.",
                    maxTokens = 40,
                    temperature = 0.3f
                )
            )
            _isTestingAi.value = false
            if (result.success) {
                _testAiResult.value = "${result.text} (Latency: ${result.latencyMs} ms)"
            } else {
                _testAiResult.value = "Test failed: ${result.errorMessage}"
            }
        }
    }

    fun resetOnboarding() {
        viewModelScope.launch {
            val old = userProfile.value
            repository.updateUserProfile(old.copy(isOnboarded = false))
        }
    }

    fun deleteAllLocalData() {
        viewModelScope.launch {
            val old = userProfile.value
            repository.updateUserProfile(old.copy(isOnboarded = false, selectedGgufPath = ""))
            unloadLocalLlamaModel()
            clearChat()
        }
    }

    fun exportLocalData(onExport: (String) -> Unit) {
        viewModelScope.launch {
            val p = userProfile.value
            val text = """
                =============== PRATIDINAM AI DATA EXPORT ===============
                User: ${p.name}
                Identity: ${p.lifeIdentity}
                Wake: ${p.wakeUpTime} / Sleep: ${p.sleepTime}
                7-Day Goal: ${p.mainSevenDayGoal}
                Weakness: ${p.biggestWeakness}
                Tone: ${p.preferredCoachTone}
                Language Mode: ${p.preferredLanguage}
                Subscription: ${p.subscriptionTier}
                =========================================================
            """.trimIndent()
            onExport(text)
        }
    }

    fun selectSubscriptionTier(tier: String) {
        viewModelScope.launch {
            val old = userProfile.value
            repository.updateUserProfile(old.copy(subscriptionTier = tier))
        }
    }

    fun saveReminderSettings(settings: ReminderSettings) {
        viewModelScope.launch {
            repository.saveReminderSettings(settings)
        }
    }

    fun resetNightAudit() {
        viewModelScope.launch {
            val dateKey = _currentDate.value
            val old = repository.getOrCreateNightAuditDirect(dateKey)
            repository.saveNightAudit(old.copy(isCompleted = false))
        }
    }

    fun getTodayDateKey(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(Calendar.getInstance().time)
    }

    fun getFormattedDisplayDate(): String {
        val sdf = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.US)
        return sdf.format(Calendar.getInstance().time)
    }
}
