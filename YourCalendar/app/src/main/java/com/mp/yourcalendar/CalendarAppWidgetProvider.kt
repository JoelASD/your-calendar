package com.mp.yourcalendar

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.RemoteViews
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*


class CalendarAppWidgetProvider : AppWidgetProvider() {

    //To check if user is logged in for the widget
    private lateinit var auth: FirebaseAuth

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

            //check, if user is logged in
            auth = FirebaseAuth.getInstance()
            val user = auth.currentUser

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
                //Also checking, if user is logged in, and if not, then displaying different TextView
                if (user == null) {
                    Log.d("LoggedIn", "im not logged in!")
                    setEmptyView(R.id.listView, R.id.empty_view_not_logged)
                    setViewVisibility(R.id.empty_view_not_logged, 0)

                } else if (user != null) {
                    Log.d("LoggedIn", "im logged in!")
                    setEmptyView(R.id.listView, R.id.empty_view)
                    setViewVisibility(R.id.empty_view, 0)

                }
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

    override fun onAppWidgetOptionsChanged(context: Context?, appWidgetManager: AppWidgetManager?, appWidgetId: Int, newOptions: Bundle?) {
        //see the dimensions :
        val options = appWidgetManager!!.getAppWidgetOptions(appWidgetId)
        Log.d("Widget", "changed Dimensions")

        //get min width and height
        val minWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)
        val minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT)

        //get the right widget and update it
   //     appWidgetManager.updateAppWidget(appWidgetId, getRemoteViews(context, appWidgetManager, appWidgetId, newOptions))
    }
}