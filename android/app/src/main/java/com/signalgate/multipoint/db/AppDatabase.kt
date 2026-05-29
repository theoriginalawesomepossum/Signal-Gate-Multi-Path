package com.signalgate.multipoint.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        PhoneEntry::class,      // New unified entity
        BlockEntry::class,      // Keep temporarily for migration
        AllowEntry::class,      // Keep temporarily
        CallLogEntry::class
    ],
    version = 4,                // Increment version
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun phoneEntryDao(): PhoneEntryDao
    abstract fun blockEntryDao(): BlockEntryDao     // Keep for now
    abstract fun allowEntryDao(): AllowEntryDao     // Keep for now

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
                .fallbackToDestructiveMigration()   // Safe for development phase
                .build()
                    .also { INSTANCE = it }
            }
        }
    }
}

// Migration example
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // New table already created via PhoneEntry entity
        db.execSQL("""
            CREATE INDEX IF NOT EXISTS index_phone_entries_phoneNumber 
            ON phone_entries(phoneNumber)
        """)
    }
}
