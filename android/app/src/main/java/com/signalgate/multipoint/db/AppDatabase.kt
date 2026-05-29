package com.signalgate.multipoint.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        PhoneEntry::class,      // Primary unified entity
        BlockEntry::class,      // Keep temporarily
        AllowEntry::class,      // Keep temporarily
        CallLogEntry::class     // Assuming this exists
    ],
    version = 4,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    // NEW - Main DAO we'll use going forward
    abstract fun phoneEntryDao(): PhoneEntryDao

    // OLD DAOs - Comment these out if they cause errors
    // abstract fun blockEntryDao(): BlockEntryDao
    // abstract fun allowEntryDao(): AllowEntryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "signalgate.db"
                )
                .addMigrations(MIGRATION_3_4)
                .enableWriteAheadLogging()
                .fallbackToDestructiveMigration()   // Critical for now during refactoring
                .build()
                    .also { INSTANCE = it }
            }
        }
    }
}

// Migration
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE INDEX IF NOT EXISTS idx_phone_entries_phoneNumber 
            ON phone_entries(phoneNumber)
        """)
    }
}
