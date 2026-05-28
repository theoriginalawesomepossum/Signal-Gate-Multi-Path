package com.signalgate.multipoint.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        PhoneEntry::class,
        // Keep BlockEntry temporarily for migration if needed
        BlockEntry::class
    ],
    version = 3,           // ← Increment from current version
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun phoneEntryDao(): PhoneEntryDao
    abstract fun blockEntryDao(): BlockEntryDao  // Keep temporarily

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
                .addMigrations(MIGRATION_2_3)
                .enableWriteAheadLogging()           // Performance
                .fallbackToDestructiveMigration()    // TEMP for development
                .build()
                    .also { INSTANCE = it }
            }
        }
    }
}

// Migration from v2 to v3: Create unified table
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Create new unified table
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS phone_entries (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                phoneNumber TEXT NOT NULL,
                action TEXT NOT NULL,
                sourceId INTEGER,
                isPattern INTEGER NOT NULL DEFAULT 0,
                confidence INTEGER NOT NULL DEFAULT 0,
                addedAt INTEGER NOT NULL DEFAULT 0,
                metadata TEXT
            )
        """)

        // Create indexes
        db.execSQL("CREATE INDEX IF NOT EXISTS index_phone_entries_phoneNumber ON phone_entries(phoneNumber)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_phone_entries_phoneNumber_action ON phone_entries(phoneNumber, action)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_phone_entries_sourceId ON phone_entries(sourceId)")

        // TODO: Later migration script to copy data from BlockEntry/AllowEntry
        // For now we use fallbackToDestructiveMigration during dev
    }
}
