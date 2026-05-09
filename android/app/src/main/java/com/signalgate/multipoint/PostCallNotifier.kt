package com.signalgate.multipoint

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

object PostCallNotifier {

    private const val CHANNEL_ID =
        "call_post_action_channel"

    private const val NOTIFICATION_ID = 1001

    fun show(
        context: Context,
        phoneNumber: String
    ) {

        val notificationManager =
            context.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val channel =
                NotificationChannel(
                    CHANNEL_ID,
                    "Post-Call Actions",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {

                    description =
                        "Actions for recently ended calls"
                }

            notificationManager
                .createNotificationChannel(channel)
        }

        val notificationBuilder =
            NotificationCompat.Builder(
                context,
                CHANNEL_ID
            )
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(
                    "Call from $phoneNumber ended"
                )
                .setContentText(
                    "Block or whitelist this number?"
                )
                .setPriority(
                    NotificationCompat.PRIORITY_HIGH
                )
                .setAutoCancel(true)

        // BLOCK ACTION

        val blockIntent =
            Intent(
                context,
                CallActionReceiver::class.java
            ).apply {

                action = "ACTION_BLOCK_PERMANENT"

                putExtra(
                    "PHONE_NUMBER",
                    phoneNumber
                )
            }

        val blockPendingIntent =
            PendingIntent.getBroadcast(
                context,
                0,
                blockIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                        PendingIntent.FLAG_IMMUTABLE
            )

        // WHITELIST ACTION

        val whitelistIntent =
            Intent(
                context,
                CallActionReceiver::class.java
            ).apply {

                action = "ACTION_WHITELIST"

                putExtra(
                    "PHONE_NUMBER",
                    phoneNumber
                )
            }

        val whitelistPendingIntent =
            PendingIntent.getBroadcast(
                context,
                1,
                whitelistIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                        PendingIntent.FLAG_IMMUTABLE
            )

        notificationBuilder.addAction(
            NotificationCompat.Action(
                0,
                "Block",
                blockPendingIntent
            )
        )

        notificationBuilder.addAction(
            NotificationCompat.Action(
                0,
                "Whitelist",
                whitelistPendingIntent
            )
        )

        notificationManager.notify(
            NOTIFICATION_ID,
            notificationBuilder.build()
        )
    }
}
