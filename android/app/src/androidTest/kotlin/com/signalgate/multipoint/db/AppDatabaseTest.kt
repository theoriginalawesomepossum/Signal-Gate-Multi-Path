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
    private lateinit var blockDao: BlockDao      // Changed to match AppDatabase
    private lateinit var allowDao: AllowDao      // Changed to match AppDatabase

    @Before
    fun createDb() {
        try {
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
                .allowMainThreadQueries() // Only for tests
                .build()

            blockDao = db.blockDao()
            allowDao = db.allowDao()
            println("✅ Test database created successfully")
        } catch (e: Exception) {
            throw AssertionError("Failed to create in-memory database", e)
        }
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        try {
            db.close()
            println("✅ Test database closed")
        } catch (e: Exception) {
            println("⚠️ Warning while closing database: ${e.message}")
        }
    }

    @Test
    fun `insert and retrieve blocked number`() = runBlocking {
        try {
            val block = BlockEntry(
                phoneNumber = "+18005551212", 
                label = "spam"           // Use 'label' instead of 'reason' if that's current
            )
            blockDao.insert(block)

            val allBlocks = blockDao.getAll()
            assertEquals("Should have exactly 1 blocked entry", 1, allBlocks.size)
            assertEquals("+18005551212", allBlocks[0].phoneNumber)
            println("✅ Blocked number insert/retrieve test passed")
        } catch (e: Exception) {
            throw AssertionError("Blocked number test failed", e)
        }
    }

    @Test
    fun `allow list works independently`() = runBlocking {
        try {
            val allow = AllowEntry(phoneNumber = "+13105551212")
            allowDao.insert(allow)

            val allAllows = allowDao.getAll()
            assertEquals("Should have exactly 1 allowed entry", 1, allAllows.size)
            println("✅ Allow list test passed")
        } catch (e: Exception) {
            throw AssertionError("Allow list test failed", e)
        }
    }

    @Test
    fun `database contains all expected DAOs`() {
        try {
            assertEquals("BlockDao should exist", true, ::blockDao.isInitialized)
            assertEquals("AllowDao should exist", true, ::allowDao.isInitialized)
            println("✅ All DAOs are accessible")
        } catch (e: Exception) {
            throw AssertionError("DAO check failed", e)
        }
    }
}
