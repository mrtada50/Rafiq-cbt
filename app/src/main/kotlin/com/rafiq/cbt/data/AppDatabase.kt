package com.rafiq.cbt.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        MoodEntry::class, ThoughtRecord::class, DownwardChain::class, ActivityEntry::class,
        ExperimentEntry::class, ExposureEntry::class, ProblemEntry::class, WorryEntry::class,
        RelapsePlanEntity::class, SettingEntry::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): CbtDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null
        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "rafiq.db"
                ).build().also { INSTANCE = it }
            }
    }
}
