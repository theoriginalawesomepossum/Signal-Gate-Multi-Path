package com.signalgate.multipoint.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [BlockEntry::class, AllowEntry::class, CallLogEntry::class, Source::class, UnifiedEntry::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun blockDao(): BlockDao
    abstract fun allowDao(): AllowDao
    abstract fun callLogDao(): CallLogDao
    abstract fun sourceDao(): SourceDao
    abstract fun unifiedEntryDao(): UnifiedEntryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "signal_gate_db"
                )
                .fallbackToDestructiveMigration() // Allows database to be recreated on version mismatch
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
