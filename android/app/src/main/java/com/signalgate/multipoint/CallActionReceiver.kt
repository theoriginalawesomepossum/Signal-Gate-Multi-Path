package com.signalgate.multipoint

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.signalgate.multipoint.database.SignalGateDatabase
import com.signalgate.multipoint.database.entities.UnifiedEntryEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CallActionReceiver : BroadcastReceiver() {

    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        val phoneNumber = intent.getStringExtra("PHONE_NUMBER") ?: return
        val action = intent.action ?: return

        scope.launch {
            val db = SignalGateDatabase.getInstance(context)
            val unifiedEntryDao = db.unifiedEntryDao()

            when (action) {
                "ACTION_BLOCK_PERMANENT" -> {
                    unifiedEntryDao.insertEntry(
                        UnifiedEntryEntity(
                            phoneNumber = phoneNumber,
                            action = "BLOCK",
                            sourceId = 1 // Assuming 1 is the MANUAL source ID
                        )
                    )
                    showToast(context, "Number blocked permanently")
                }
                "ACTION_WHITELIST" -> {
                    unifiedEntryDao.insertEntry(
                        UnifiedEntryEntity(
                            phoneNumber = phoneNumber,
                            action = "ALLOW",
                            sourceId = 1 // Assuming 1 is the MANUAL source ID
                        )
                    )
                    showToast(context, "Number added to whitelist")
                }
                "ACTION_BLOCK_PREFIX" -> {
                    unifiedEntryDao.insertEntry(
                        UnifiedEntryEntity(
                            phoneNumber = phoneNumber,
                            action = "BLOCK",
                            sourceId = 1,
                            isPattern = true
                        )
                    )
                    showToast(context, "Prefix blocked")
                }
                "ACTION_BLOCK_AREA_CODE" -> {
                    val areaCode = phoneNumber.take(4) // first 4 digits as area code
                    unifiedEntryDao.insertEntry(
                        UnifiedEntryEntity(
                            phoneNumber = areaCode,
                            action = "BLOCK",
                            sourceId = 1,
                            isPattern = true
                        )
                    )
                    showToast(context, "Area code blocked")
                }
                "ACTION_IGNORE" -> {
                    showToast(context, "Call ignored")
                }
            }
        }
    }

    private fun showToast(context: Context, message: String) {
        CoroutineScope(Dispatchers.Main).launch {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }
}
