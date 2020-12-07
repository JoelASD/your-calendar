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

        //.setContentTitle(intent.getStringExtra("title", ""))
        //.setContentText(intent.getStringExtra("text", ""))
        /*val notification: Notification = NotificationCompat.Builder(context)
            .setSmallIcon(R.drawable.ic_calendar_icon_24)
            .setContentTitle("Title")
            .setContentText("TEXTIA")
            .build()
        // Show built notification
        //val manager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        Log.d("RECEIVER", "NOTIF BUILD")
        val notificationManager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(42, notification)*/
    }
}