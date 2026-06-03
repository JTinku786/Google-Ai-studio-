package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyRoutineDao {

    // --- Daily Routine States (Legacy/Core checklist tracking) ---
    @Query("SELECT * FROM daily_routines WHERE date = :date")
    fun getRoutineState(date: String): Flow<DailyRoutineState?>

    @Query("SELECT * FROM daily_routines WHERE date = :date")
    suspend fun getRoutineStateDirect(date: String): DailyRoutineState?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(state: DailyRoutineState)

    @Query("SELECT * FROM daily_routines ORDER BY date DESC")
    fun getAllHistory(): Flow<List<DailyRoutineState>>


    // --- User Profile ---
    @Query("SELECT * FROM user_profile WHERE id = 1")
    fun getUserProfileFlow(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profile WHERE id = 1")
    suspend fun getUserProfileDirect(): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateUserProfile(profile: UserProfile)


    // --- Habit & Habit Log ---
    @Query("SELECT * FROM habits WHERE isActive = 1")
    fun getActiveHabits(): Flow<List<Habit>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: Habit)

    @Query("DELETE FROM habits WHERE id = :id")
    suspend fun deleteHabit(id: Long)

    @Query("SELECT * FROM habit_logs WHERE date = :date")
    fun getHabitLogsForDate(date: String): Flow<List<HabitLog>>

    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId ORDER BY date DESC")
    fun getLogsForHabit(habitId: Long): Flow<List<HabitLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateHabitLog(log: HabitLog)


    // --- Journal ---
    @Query("SELECT * FROM journal_entries WHERE date = :date")
    fun getJournalEntriesForDate(date: String): Flow<List<JournalEntry>>

    @Query("SELECT * FROM journal_entries WHERE date = :date AND type = :type LIMIT 1")
    suspend fun getJournalEntryDirect(date: String, type: String): JournalEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJournalEntry(entry: JournalEntry)

    @Query("SELECT * FROM journal_entries ORDER BY createdAt DESC")
    fun getAllJournalEntries(): Flow<List<JournalEntry>>


    // --- Meal Logs ---
    @Query("SELECT * FROM meal_logs WHERE date = :date ORDER BY createdAt ASC")
    fun getMealsForDate(date: String): Flow<List<MealLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMealLog(meal: MealLog)

    @Query("DELETE FROM meal_logs WHERE id = :id")
    suspend fun deleteMealLog(id: Long)


    // --- Daily Rich Content (Quotes, Slokas, Tips, Financial, Learning Topics) ---
    @Query("SELECT * FROM daily_contents WHERE date = :date")
    suspend fun getDailyContent(date: String): DailyContent?

    @Query("SELECT * FROM daily_contents WHERE date = :date")
    fun getDailyContentFlow(date: String): Flow<DailyContent?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyContent(content: DailyContent)


    // --- Voice Notes ---
    @Query("SELECT * FROM voice_notes ORDER BY createdAt DESC")
    fun getAllVoiceNotes(): Flow<List<VoiceNote>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVoiceNote(note: VoiceNote)

    @Query("DELETE FROM voice_notes WHERE id = :id")
    suspend fun deleteVoiceNote(id: Long)


    // --- Contact Reminders ---
    @Query("SELECT * FROM contact_reminders ORDER BY contactName ASC")
    fun getAllContactReminders(): Flow<List<ContactReminder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContactReminder(reminder: ContactReminder)

    @Query("DELETE FROM contact_reminders WHERE id = :id")
    suspend fun deleteContactReminder(id: Long)

    // --- Daily Mission (Morning/Today's Mission) ---
    @Query("SELECT * FROM daily_missions WHERE date = :date")
    fun getDailyMission(date: String): Flow<DailyMission?>

    @Query("SELECT * FROM daily_missions WHERE date = :date")
    suspend fun getDailyMissionDirect(date: String): DailyMission?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateDailyMission(mission: DailyMission)


    // --- Night Audit (Evening Reflection) ---
    @Query("SELECT * FROM night_audits WHERE date = :date")
    fun getNightAudit(date: String): Flow<NightAudit?>

    @Query("SELECT * FROM night_audits WHERE date = :date")
    suspend fun getNightAuditDirect(date: String): NightAudit?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateNightAudit(audit: NightAudit)

    @Query("SELECT * FROM night_audits ORDER BY date DESC")
    fun getAllNightAudits(): Flow<List<NightAudit>>

    @Query("SELECT * FROM night_audits ORDER BY date DESC")
    suspend fun getAllNightAuditsDirect(): List<NightAudit>


    // --- Reminder Settings ---
    @Query("SELECT * FROM reminder_settings WHERE id = 1")
    fun getReminderSettings(): Flow<ReminderSettings?>

    @Query("SELECT * FROM reminder_settings WHERE id = 1")
    suspend fun getReminderSettingsDirect(): ReminderSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateReminderSettings(settings: ReminderSettings)
}
