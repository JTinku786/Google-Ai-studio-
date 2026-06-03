package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Long = 1,
    val name: String = "Reddy",
    val wakeUpTime: String = "06:00 AM",
    val sleepTime: String = "10:30 PM",
    val preferredLanguage: String = "Telugu",
    val foodPreference: String = "Non-Vegetarian",
    val waterGoalMl: Int = 2500,
    val ollamaBaseUrl: String = "http://127.0.0.1:11434/api",
    val ollamaModel: String = "llama3.2:1b",
    val selectedGgufPath: String = "",
    val ggufContextSize: Int = 512,
    val ggufMaxTokens: Int = 120,
    val ggufTemperature: Float = 0.3f,
    val ggufThreads: Int = 4,
    val ggufBatchSize: Int = 128,
    val isOnboarded: Boolean = false,
    val lifeIdentity: String = "Disciplined Founder",
    val mainSevenDayGoal: String = "Achieve perfect routine",
    val biggestWeakness: String = "Laziness",
    val preferredCoachTone: String = "Strict",
    val subscriptionTier: String = "FREE"
)

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String, // Health, Mind, Finance, etc.
    val targetFrequency: String = "Daily",
    val isActive: Boolean = true
)

@Entity(tableName = "habit_logs")
data class HabitLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val habitId: Long,
    val date: String, // Format: YYYY-MM-DD
    val status: String, // Completed, Skipped
    val note: String? = null
)

@Entity(tableName = "journal_entries")
data class JournalEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String, // Format: YYYY-MM-DD
    val type: String, // Plan, Morning, Evening, Reflection
    val content: String,
    val mood: String? = null,
    val energyLevel: Int? = null,
    val aiSummary: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "meal_logs")
data class MealLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String, // Format: YYYY-MM-DD
    val mealType: String, // Breakfast, Lunch, Dinner, Snack
    val description: String,
    val imageUri: String? = null,
    val estimatedCalories: Int? = null,
    val estimatedProtein: Int? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "daily_contents")
data class DailyContent(
    @PrimaryKey val date: String, // Format: YYYY-MM-DD
    val quote: String,
    val gitaSloka: String,
    val gitaMeaningEnglish: String,
    val gitaMeaningTelugu: String,
    val teluguChitka: String,
    val financialTopic: String,
    val financialExplanation: String,
    val learningTopic: String,
    val learningExplanation: String
)

@Entity(tableName = "voice_notes")
data class VoiceNote(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String, // Format: YYYY-MM-DD
    val audioUri: String,
    val transcript: String? = null,
    val aiSummary: String? = null,
    val category: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "contact_reminders")
data class ContactReminder(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val contactName: String,
    val phoneNumber: String,
    val category: String, // Family, Friends, Business
    val lastContactedAt: Long? = null,
    val reminderFrequency: String = "Weekly",
    val notes: String? = null
)

@Entity(tableName = "daily_missions")
data class DailyMission(
    @PrimaryKey val date: String, // YYYY-MM-DD
    val wakeUpStatus: String = "NOT_LOGGED", // e.g., On Time, Late, Excused
    val disciplineScore: Int = 0,
    val missionOne: String = "Complete critical work block",
    val missionTwo: String = "Stay hydrated and avoid junk",
    val missionThree: String = "Call or connect with family",
    val waterTargetMl: Int = 2500,
    val waterConsumedMl: Int = 0,
    val hardestTask: String = "Finish primary priority first thing",
    val hardestTaskCompleted: Boolean = false,
    val aiMorningCommand: String = "Win the morning to conquer the day.",
    val wisdomQuote: String = "Perform work without attachment.",
    val familyReminder: String = "Have you checked in on Amma today?",
    val foodSuggestion: String = "Start with healthy hydration and Jowar/Ragi Java.",
    val isGenerated: Boolean = false
)

@Entity(tableName = "night_audits")
data class NightAudit(
    @PrimaryKey val date: String, // YYYY-MM-DD
    val wokeUpOnTime: Boolean = false,
    val hardestTaskCompleted: Boolean = false,
    val waterConsumedMl: Int = 0,
    val foodQuality: String = "Okay", // Poor, Okay, Good, Great
    val mood: String = "Normal",
    val energyLevel: Int = 5, // 1-10
    val whatWentWell: String = "",
    val whatFailed: String = "",
    val oneThingToImprove: String = "",
    val aiReflection: String = "",
    val tomorrowCorrection: String = "",
    val isCompleted: Boolean = false
)

@Entity(tableName = "reminder_settings")
data class ReminderSettings(
    @PrimaryKey val id: Long = 1,
    val wakeUpReminderEnabled: Boolean = true,
    val wakeUpReminderTime: String = "06:00",
    val waterReminderEnabled: Boolean = true,
    val waterIntervalMinutes: Int = 120,
    val hardestTaskReminderEnabled: Boolean = true,
    val hardestTaskReminderTime: String = "09:00",
    val eveningReflectionReminderEnabled: Boolean = true,
    val eveningReflectionTime: String = "21:30",
    val sleepWindDownReminderEnabled: Boolean = true,
    val sleepWindDownTime: String = "22:00",
    val familyConnectionReminderEnabled: Boolean = true,
    val familyConnectionTime: String = "18:30"
)
