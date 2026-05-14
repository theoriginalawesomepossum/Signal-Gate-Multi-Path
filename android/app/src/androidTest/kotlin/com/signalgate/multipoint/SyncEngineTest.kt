package com.signalgate.multipoint

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.signalgate.multipoint.db.AppDatabase
import com.signalgate.multipoint.db.Source
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class SyncEngineTest {

    private lateinit var db: AppDatabase
    private lateinit var syncEngine: SyncEngine

    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        syncEngine = SyncEngine(context)
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun `sync small csv file`() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val testFile = File(context.cacheDir, "test_blocks.csv")
        testFile.writeText("+18005551212,BLOCK,false\n+12125550000,ALLOW,false")

        val source = Source(id = 1, name = "Test Source", type = "LOCAL_FILE", pathOrUrl = testFile.absolutePath)
        
        syncEngine.syncSource(source)

        val entries = db.unifiedEntryDao().findByNumber("+18005551212")
        assertEquals(1, entries.size)
        assertEquals("BLOCK", entries[0].action)
        
        testFile.delete()
    }

    @Test
    fun `sync respects benchmark limit`() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val testFile = File(context.cacheDir, "limit_test.csv")
        val sb = StringBuilder()
        for (i in 1..100) {
            sb.append("+10000000$i,BLOCK,false\n")
        }
        testFile.writeText(sb.toString())

        val source = Source(id = 2, name = "Limit Test", type = "LOCAL_FILE", pathOrUrl = testFile.absolutePath)
        
        // Sync with a limit of 50
        syncEngine.syncSource(source, limit = 50)

        val count = db.unifiedEntryDao().getAllPatterns().size // Patterns check is just a proxy here
        // We need a proper count method in DAO, but for now we'll check a specific entry
        val entry50 = db.unifiedEntryDao().findByNumber("+1000000050")
        val entry51 = db.unifiedEntryDao().findByNumber("+1000000051")
        
        assertEquals(1, entry50.size)
        assertEquals(0, entry51.size)
        
        testFile.delete()
    }
}
