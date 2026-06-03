package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        DailyRoutineState::class,
        UserProfile::class,
        Habit::class,
        HabitLog::class,
        JournalEntry::class,
        MealLog::class,
        DailyContent::class,
        VoiceNote::class,
        ContactReminder::class,
        DailyMission::class,
        NightAudit::class,
        ReminderSettings::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dailyRoutineDao(): DailyRoutineDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "daily_routine_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
