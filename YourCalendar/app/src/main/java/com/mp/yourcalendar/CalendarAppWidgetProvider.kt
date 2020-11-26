package com.mp.yourcalendar

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.mp.yourcalendar.ui.home.HomeFragment

class CalendarAppWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        //for each App widget, that belongs to the provider
        appWidgetIds.forEach { appWidgetId ->
            //create Intent to launch ExampleActivity
            val pendingIntent: PendingIntent = Intent(context, MainActivity::class.java)
                .let { intent ->
                    PendingIntent.getActivity(context, 0, intent, 0)
                }
            //get the layout for the App Widget and attach on click listener to the button
            val views: RemoteViews = RemoteViews(context.packageName, R.layout.event_appwidget).apply {
                setOnClickPendingIntent(R.id.clickTextView, pendingIntent)
            }
            //tell the Appwidgetmanager to perform an update on current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views)

        }
    }
}