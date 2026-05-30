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
    val ggufBatchSize: Int = 128
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
