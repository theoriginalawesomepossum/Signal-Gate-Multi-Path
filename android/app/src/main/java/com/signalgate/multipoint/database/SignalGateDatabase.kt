package com.signalgate.multipoint.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.signalgate.multipoint.database.daos.CallLogDao
import com.signalgate.multipoint.database.daos.SettingDao
import com.signalgate.multipoint.database.daos.SourceDao
import com.signalgate.multipoint.database.daos.SyncHistoryDao
import com.signalgate.multipoint.database.daos.UnifiedEntryDao
import com.signalgate.multipoint.database.entities.CallLogEntry
import com.signalgate.multipoint.database.entities.SettingEntry
import com.signalgate.multipoint.database.entities.SourceEntity
import com.signalgate.multipoint.database.entities.SyncHistoryEntry
import com.signalgate.multipoint.database.entities.UnifiedEntryEntity

/**
 * SignalGateDatabase is the main Room database for the SignalGate Multi-Port application.
 * It manages all data related to sources, entries, call logs, settings, and sync history.
 */
@Database(
    entities = [
        SourceEntity::class,
        UnifiedEntryEntity::class,
        CallLogEntry::class,
        SettingEntry::class,
        SyncHistoryEntry::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SignalGateDatabase : RoomDatabase() {
    abstract fun sourceDao(): SourceDao
    abstract fun unifiedEntryDao(): UnifiedEntryDao
    abstract fun callLogDao(): CallLogDao
    abstract fun settingDao(): SettingDao
    abstract fun syncHistoryDao(): SyncHistoryDao

    companion object {
        private const val DATABASE_NAME = "signalgate_multiport.db"
        private var instance: SignalGateDatabase? = null

        fun getInstance(context: Context): SignalGateDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context): SignalGateDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                SignalGateDatabase::class.java,
                DATABASE_NAME
            )
                .enableMultiInstanceInvalidation()
                .addMigrations(
                    // Add migrations here as the schema evolves
                )
                .build()
        }

        fun closeDatabase() {
            instance?.close()
            instance = null
        }
    }
}

/**
 * Migration example (for future schema updates).
 * This is a placeholder for database migrations.
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Example migration: add a new column
        // database.execSQL("ALTER TABLE sources ADD COLUMN new_column TEXT DEFAULT NULL")
    }
}
