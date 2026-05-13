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
                    db.blockDao().insert(BlockEntry(phoneNumber = phoneNumber))
                    showToast(context, "Number blocked permanently")
                }
                "ACTION_WHITELIST" -> {
                    db.allowDao().insert(com.signalgate.multipoint.db.AllowEntry(phoneNumber = phoneNumber))
                    showToast(context, "Number added to whitelist")
                }
                "ACTION_BLOCK_PREFIX" -> {
                    db.blockDao().insert(BlockEntry(phoneNumber = phoneNumber, isPattern = true))
                    showToast(context, "Prefix blocked")
                }
                "ACTION_BLOCK_AREA_CODE" -> {
                    val areaCode = phoneNumber.take(4) // first 4 digits as area code
                    db.blockDao().insert(BlockEntry(phoneNumber = areaCode, isPattern = true))
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
