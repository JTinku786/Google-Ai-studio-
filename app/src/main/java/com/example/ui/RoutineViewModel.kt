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

        val systemPrompt = """
            You are a grounded personal lifestyle AI coach specifically styled as '${coachRole}'.
            Your purpose is to improve the user's discipline, health, mental peace, learning, financial literacy, and social/family habits.
            Be practical, wise, and culturally comforting to a Telangana/Telugu background user named '${profile.name}'.
            Keep responses short, actionable, and visually clear. Do not provide diagnostic medical warnings, or personalized banking financial advice.
            Respect Telugu traditions while staying rational. Speak with calm strength.
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
                    finalResponse = generateOfflineResponseFallback(text, coachRole) + "\n\n💡 (Local GGUF engine error: ${result.errorMessage}. Loaded offline fallback.)"
                }
            } else {
                finalResponse = generateOfflineResponseFallback(text, coachRole) + "\n\n💡 (Local GGUF model is not loaded in Settings. Loaded offline fallback.)"
            }
            val updatedHistory = _chatHistory.value.toMutableList()
            updatedHistory.add(Pair("assistant", finalResponse))
            _chatHistory.value = updatedHistory
            _isChatGenerating.value = false
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

    fun getTodayDateKey(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(Calendar.getInstance().time)
    }

    fun getFormattedDisplayDate(): String {
        val sdf = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.US)
        return sdf.format(Calendar.getInstance().time)
    }
}
