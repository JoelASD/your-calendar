package com.mp.yourcalendar

import android.app.Notification
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context

import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.getSystemService


class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Build notification
        val builder: NotificationCompat.Builder = NotificationCompat.Builder(context, "1")
            .setSmallIcon(R.drawable.ic_calendar_icon_24)
            .setContentTitle(intent.getStringExtra("title"))
            .setContentText(intent.getStringExtra("description"))
        with(NotificationManagerCompat.from(context)){
            Log.d("RECEIVER", "NOTIF BUILD")
            notify(1, builder.build())
        }
    }
}