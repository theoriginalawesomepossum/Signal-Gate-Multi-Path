package com.signalgate.multipoint.db

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class AppDatabaseTest {

    private lateinit var db: AppDatabase
    private lateinit var unifiedEntryDao: UnifiedEntryDao
    private lateinit var sourceDao: SourceDao

    @Before
    fun createDb() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        unifiedEntryDao = db.unifiedEntryDao()
        sourceDao = db.sourceDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun `insert and retrieve unified entry`() = runBlocking {
        val entry = UnifiedEntry(
            phoneNumber = "+18005551212",
            action = "BLOCK",
            sourceId = 0 // Manual
        )
        unifiedEntryDao.insert(entry)

        val matches = unifiedEntryDao.findByNumber("+18005551212")
        assertEquals(1, matches.size)
        assertEquals("BLOCK", matches[0].action)
    }

    @Test
    fun `source priority sorting`() = runBlocking {
        sourceDao.insert(Source(name = "Low Priority", type = "URL", pathOrUrl = "url1", priority = 100))
        sourceDao.insert(Source(name = "High Priority", type = "URL", pathOrUrl = "url2", priority = 10))

        val allSources = sourceDao.getAll()
        assertEquals("High Priority", allSources[0].name)
        assertEquals("Low Priority", allSources[1].name)
    }
}
