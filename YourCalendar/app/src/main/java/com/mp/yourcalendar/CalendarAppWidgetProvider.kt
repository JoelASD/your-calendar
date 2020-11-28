package com.mp.yourcalendar

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.RemoteViews
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import com.mp.yourcalendar.ui.home.HomeFragment
import java.text.SimpleDateFormat
import java.util.*


class CalendarAppWidgetProvider : AppWidgetProvider() {

    companion object HomeFragment {
        var eventList: MutableList<Event> = mutableListOf()
        var properList: MutableList<Event> = mutableListOf()

    }

    // Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var ref: DatabaseReference

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

            // Set up the intent that starts the EventWidgetService, which will
            // provide the views for this collection.
            val intent = Intent(context, EventWidgetService::class.java).apply {
                // Add the app widget ID to the intent extras.
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
            }

            //get the layout for the App Widget and attach on click listener to the button
            val views: RemoteViews = RemoteViews(context.packageName, R.layout.event_appwidget).apply {
                setOnClickPendingIntent(R.id.clickTextView, pendingIntent)

                // Set up the RemoteViews object to use a RemoteViews adapter.
                // This adapter connects
                // to a RemoteViewsService  through the specified intent.
                // This is how you populate the data.
                setRemoteAdapter(R.id.listView, intent)

                // The empty view is displayed when the collection has no items.
                // It should be in the same layout used to instantiate the RemoteViews
                // object above.
                //setEmptyView(R.id.listView, R.id.empty_view)
            }
            //just a test for updating day
            loadDateView(views)
            //tell the Appwidgetmanager to perform an update on current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }

    //Setting the date of the top of the widget
    fun loadDateView(views: RemoteViews){
        val weekday = SimpleDateFormat("EEEE, dd")
        views.setTextViewText(R.id.dayTextView, weekday.format(Date()))
    }
}