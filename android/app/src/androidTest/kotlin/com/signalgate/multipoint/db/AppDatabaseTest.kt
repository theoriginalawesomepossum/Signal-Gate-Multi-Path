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
    private lateinit var blockDao: BlockDao
    private lateinit var allowDao: AllowDao

    @Before
    fun createDb() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries() // for tests only
            .build()
        blockDao = db.blockDao()
        allowDao = db.allowDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertAndRetrieveBlockedNumber() = runBlocking {
        val block = BlockEntry(phoneNumber = "+18005551212", label = "spam")
        blockDao.insert(block)

        val allBlocks = blockDao.getAll()
        assertEquals(1, allBlocks.size)
        assertEquals("+18005551212", allBlocks[0].phoneNumber)
    }

    @Test
    fun allowListWorksIndependently() = runBlocking {
        val allow = AllowEntry(phoneNumber = "+13105551212")
        allowDao.insert(allow)

        val allAllows = allowDao.getAll()
        assertEquals(1, allAllows.size)
    }
}
