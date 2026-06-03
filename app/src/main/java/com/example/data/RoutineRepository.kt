package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class RoutineRepository(private val dailyRoutineDao: DailyRoutineDao) {

    // --- Legacy / Core Checklist State ---
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
        dailyRoutineDao.insertOrUpdate(current.copy(brush = completed, lastUpdated = System.currentTimeMillis()))
    }

    suspend fun updateBath(date: String, completed: Boolean) {
        val current = getOrCreateRoutineStateDirect(date)
        dailyRoutineDao.insertOrUpdate(current.copy(bath = completed, lastUpdated = System.currentTimeMillis()))
    }

    suspend fun updateTradingTime(date: String, completed: Boolean) {
        val current = getOrCreateRoutineStateDirect(date)
        dailyRoutineDao.insertOrUpdate(current.copy(tradingTime = completed, lastUpdated = System.currentTimeMillis()))
    }

    suspend fun updateTradingLiquidity(date: String, completed: Boolean) {
        val current = getOrCreateRoutineStateDirect(date)
        dailyRoutineDao.insertOrUpdate(current.copy(tradingLiquidity = completed, lastUpdated = System.currentTimeMillis()))
    }

    suspend fun updateTradingDisplacement(date: String, completed: Boolean) {
        val current = getOrCreateRoutineStateDirect(date)
        dailyRoutineDao.insertOrUpdate(current.copy(tradingDisplacement = completed, lastUpdated = System.currentTimeMillis()))
    }

    suspend fun updatePuzzleSolved(date: String, completed: Boolean) {
        val current = getOrCreateRoutineStateDirect(date)
        dailyRoutineDao.insertOrUpdate(current.copy(puzzleSolved = completed, lastUpdated = System.currentTimeMillis()))
    }

    suspend fun updateWaterIntake(date: String, amountMl: Int) {
        val current = getOrCreateRoutineStateDirect(date)
        dailyRoutineDao.insertOrUpdate(current.copy(waterIntakeMl = amountMl, lastUpdated = System.currentTimeMillis()))
    }

    fun getAllHistory(): Flow<List<DailyRoutineState>> {
        return dailyRoutineDao.getAllHistory()
    }


    // --- User Profile ---
    fun getUserProfileFlow(): Flow<UserProfile?> {
        return dailyRoutineDao.getUserProfileFlow()
    }

    suspend fun getOrCreateUserProfileDirect(): UserProfile {
        val profile = dailyRoutineDao.getUserProfileDirect()
        if (profile == null) {
            val defaultProfile = UserProfile(
                id = 1,
                name = "Reddy",
                wakeUpTime = "06:00 AM",
                sleepTime = "10:30 PM",
                preferredLanguage = "Telugu",
                foodPreference = "Non-Vegetarian",
                waterGoalMl = 2500,
                ollamaBaseUrl = "http://127.0.0.1:11434/api",
                ollamaModel = "llama3.2:1b"
            )
            dailyRoutineDao.insertOrUpdateUserProfile(defaultProfile)
            return defaultProfile
        }
        return profile
    }

    suspend fun updateUserProfile(profile: UserProfile) {
        dailyRoutineDao.insertOrUpdateUserProfile(profile)
    }


    // --- Habit & Habit Log ---
    fun getActiveHabits(): Flow<List<Habit>> {
        return dailyRoutineDao.getActiveHabits()
    }

    suspend fun preseedHabitsIfEmpty() {
        val habits = dailyRoutineDao.getActiveHabits().firstOrNull() ?: emptyList()
        if (habits.isEmpty()) {
            val defaultHabits = listOf(
                Habit(name = "Wake up early", category = "Discipline"),
                Habit(name = "Drink 500 ml water", category = "Health"),
                Habit(name = "Cold shower", category = "Health"),
                Habit(name = "Deep breathing & Meditation", category = "Mind"),
                Habit(name = "Think about daily goals", category = "Mind"),
                Habit(name = "Write daily journal", category = "Mind/Growth"),
                Habit(name = "Healthy breakfast", category = "Health"),
                Habit(name = "Physical exercise / Gym", category = "Health"),
                Habit(name = "Learn financial topic", category = "Finance"),
                Habit(name = "Learn something new", category = "Growth"),
                Habit(name = "Call family/friend", category = "Relationships"),
                Habit(name = "No unnecessary scrolling", category = "Discipline"),
                Habit(name = "Sleep on time", category = "Sleep")
            )
            for (habit in defaultHabits) {
                dailyRoutineDao.insertHabit(habit)
            }
        }
    }

    suspend fun insertHabit(habit: Habit) {
        dailyRoutineDao.insertHabit(habit)
    }

    suspend fun deleteHabit(id: Long) {
        dailyRoutineDao.deleteHabit(id)
    }

    fun getHabitLogsForDate(date: String): Flow<List<HabitLog>> {
        return dailyRoutineDao.getHabitLogsForDate(date)
    }

    fun getLogsForHabit(habitId: Long): Flow<List<HabitLog>> {
        return dailyRoutineDao.getLogsForHabit(habitId)
    }

    suspend fun toggleHabitLog(habitId: Long, date: String, status: String, note: String? = null) {
        val log = HabitLog(habitId = habitId, date = date, status = status, note = note)
        dailyRoutineDao.insertOrUpdateHabitLog(log)
    }


    // --- Journal Entry ---
    fun getJournalEntriesForDate(date: String): Flow<List<JournalEntry>> {
        return dailyRoutineDao.getJournalEntriesForDate(date)
    }

    suspend fun getJournalEntryDirect(date: String, type: String): JournalEntry? {
        return dailyRoutineDao.getJournalEntryDirect(date, type)
    }

    suspend fun insertJournalEntry(entry: JournalEntry) {
        dailyRoutineDao.insertJournalEntry(entry)
    }

    fun getAllJournalEntries(): Flow<List<JournalEntry>> {
        return dailyRoutineDao.getAllJournalEntries()
    }


    // --- Meal Log ---
    fun getMealsForDate(date: String): Flow<List<MealLog>> {
        return dailyRoutineDao.getMealsForDate(date)
    }

    suspend fun insertMealLog(meal: MealLog) {
        dailyRoutineDao.insertMealLog(meal)
    }

    suspend fun deleteMealLog(id: Long) {
        dailyRoutineDao.deleteMealLog(id)
    }


    // --- Rich Daily Content ---
    fun getDailyContentFlow(date: String): Flow<DailyContent?> {
        return dailyRoutineDao.getDailyContentFlow(date)
    }

    suspend fun getOrCreateDailyContentDirect(date: String): DailyContent {
        val existing = dailyRoutineDao.getDailyContent(date)
        if (existing == null) {
            val content = DailyRoutineData.getDailyContentForDate(date)
            dailyRoutineDao.insertDailyContent(content)
            return content
        }
        return existing
    }

    suspend fun saveDailyContent(content: DailyContent) {
        dailyRoutineDao.insertDailyContent(content)
    }


    // --- Voice Notes ---
    fun getAllVoiceNotes(): Flow<List<VoiceNote>> {
        return dailyRoutineDao.getAllVoiceNotes()
    }

    suspend fun insertVoiceNote(note: VoiceNote) {
        dailyRoutineDao.insertVoiceNote(note)
    }

    suspend fun deleteVoiceNote(id: Long) {
        dailyRoutineDao.deleteVoiceNote(id)
    }


    // --- Contact Reminders ---
    fun getAllContactReminders(): Flow<List<ContactReminder>> {
        return dailyRoutineDao.getAllContactReminders()
    }

    suspend fun preseedContactsIfEmpty() {
        val contacts = dailyRoutineDao.getAllContactReminders().firstOrNull() ?: emptyList()
        if (contacts.isEmpty()) {
            val defaults = listOf(
                ContactReminder(contactName = "Amma (Mother)", phoneNumber = "+919490000001", category = "Family", reminderFrequency = "Daily", notes = "Tell Amma about your well-being."),
                ContactReminder(contactName = "Nanna (Father)", phoneNumber = "+919490000002", category = "Family", reminderFrequency = "Weekly", notes = "Ask about agriculture/work."),
                ContactReminder(contactName = "Ravi (Best Friend)", phoneNumber = "+919848011111", category = "Friends", reminderFrequency = "Weekly", notes = "Catch up on life / business."),
                ContactReminder(contactName = "Srinivas Broker (Business)", phoneNumber = "+919848022222", category = "Business", reminderFrequency = "Monthly", notes = "Inquire about land trends in Hyderabad.")
            )
            for (contact in defaults) {
                dailyRoutineDao.insertContactReminder(contact)
            }
        }
    }

    suspend fun insertContactReminder(reminder: ContactReminder) {
        dailyRoutineDao.insertContactReminder(reminder)
    }

    suspend fun deleteContactReminder(id: Long) {
        dailyRoutineDao.deleteContactReminder(id)
    }

    // --- Daily Mission Methods ---
    fun getDailyMission(date: String): Flow<DailyMission?> {
        return dailyRoutineDao.getDailyMission(date)
    }

    suspend fun getOrCreateDailyMissionDirect(date: String, profile: UserProfile): DailyMission {
        val existing = dailyRoutineDao.getDailyMissionDirect(date)
        if (existing == null) {
            val defaultMission = DailyMission(
                date = date,
                waterTargetMl = profile.waterGoalMl,
                hardestTask = "Complete your morning focus session",
                wisdomQuote = "Perform work without attachment.",
                familyReminder = "Have you connected with Amma today?"
            )
            dailyRoutineDao.insertOrUpdateDailyMission(defaultMission)
            return defaultMission
        }
        return existing
    }

    suspend fun saveDailyMission(mission: DailyMission) {
        dailyRoutineDao.insertOrUpdateDailyMission(mission)
    }

    // --- Night Audit Methods ---
    fun getNightAudit(date: String): Flow<NightAudit?> {
        return dailyRoutineDao.getNightAudit(date)
    }

    suspend fun getOrCreateNightAuditDirect(date: String): NightAudit {
        val existing = dailyRoutineDao.getNightAuditDirect(date)
        if (existing == null) {
            val defaultAudit = NightAudit(date = date)
            dailyRoutineDao.insertOrUpdateNightAudit(defaultAudit)
            return defaultAudit
        }
        return existing
    }

    suspend fun saveNightAudit(audit: NightAudit) {
        dailyRoutineDao.insertOrUpdateNightAudit(audit)
    }

    fun getAllNightAudits(): Flow<List<NightAudit>> {
        return dailyRoutineDao.getAllNightAudits()
    }

    suspend fun getAllNightAuditsDirect(): List<NightAudit> {
        return dailyRoutineDao.getAllNightAuditsDirect()
    }

    // --- Reminder Settings Methods ---
    fun getReminderSettingsFlow(): Flow<ReminderSettings?> {
        return dailyRoutineDao.getReminderSettings()
    }

    suspend fun getOrCreateReminderSettings(): ReminderSettings {
        val existing = dailyRoutineDao.getReminderSettingsDirect()
        if (existing == null) {
            val defaultSettings = ReminderSettings()
            dailyRoutineDao.insertOrUpdateReminderSettings(defaultSettings)
            return defaultSettings
        }
        return existing
    }

    suspend fun saveReminderSettings(settings: ReminderSettings) {
        dailyRoutineDao.insertOrUpdateReminderSettings(settings)
    }
}
