package com.signalgate.multipoint

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.signalgate.multipoint.db.AppDatabase
import com.signalgate.multipoint.db.BlockEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CallActionReceiver : BroadcastReceiver() {

    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        val phoneNumber = intent.getStringExtra("PHONE_NUMBER") ?: return
        val action = intent.action ?: return

        scope.launch {
            val db = AppDatabase.getDatabase(context)

            when (action) {
                "ACTION_BLOCK_PERMANENT" -> {
                    db.blockDao().insert(BlockEntry(phoneNumber = phoneNumber, reason = "User blocked after call"))
                    showToast(context, "Number blocked permanently: $phoneNumber")
                }
                "ACTION_WHITELIST" -> {
                    db.allowDao().insert(com.signalgate.multipoint.db.AllowEntry(phoneNumber = phoneNumber))
                    showToast(context, "Number whitelisted: $phoneNumber")
                }
                "ACTION_BLOCK_PREFIX" -> {
                    db.blockDao().insert(BlockEntry(phoneNumber = phoneNumber, reason = "User blocked prefix", isPattern = true))
                    showToast(context, "Prefix blocked: $phoneNumber")
                }
                "ACTION_BLOCK_AREA_CODE" -> {
                    // For simplicity, treat area code as prefix of the normalized number (first 3-4 digits)
                    val areaCode = phoneNumber.take(4) // adjust if you want exact area code logic
                    db.blockDao().insert(BlockEntry(phoneNumber = areaCode, reason = "User blocked area code", isPattern = true))
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
