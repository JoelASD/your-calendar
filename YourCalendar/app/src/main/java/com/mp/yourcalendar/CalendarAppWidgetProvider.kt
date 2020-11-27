package com.mp.yourcalendar

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.mp.yourcalendar.ui.home.HomeFragment
import java.text.SimpleDateFormat
import java.util.*


class CalendarAppWidgetProvider : AppWidgetProvider() {

    companion object HomeFragment {
        var eventList: MutableList<Event> = mutableListOf()
        var properList: MutableList<Event> = mutableListOf()

    }

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
            //just a test for updating day
            loadDateView(views)
         //   test123(views)
            //tell the Appwidgetmanager to perform an update on current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views)

        }
    }

    //Setting the date of the top of the widget
    fun loadDateView(views: RemoteViews){
        val weekday = SimpleDateFormat("EEEE, dd")
        views.setTextViewText(R.id.dayTextView, weekday.format(Date()))
    }

    fun test123(views: RemoteViews) {
            val strangeDate = SimpleDateFormat("dd/MM/yyyy")
        val testString = strangeDate.format(Date())
        for (event in properList) {
            if(event.eventStartDate == testString){
                views.setTextViewText(R.id.dayTextView, testString)
            }
        }
    }

    fun getEventList(date: String) {
        val widgetList: MutableList<Event> = mutableListOf()
        for (event in properList){
            if (event.eventStartDate == date) {
                widgetList.add(event)
            }
        }
    }
}