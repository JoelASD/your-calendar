package com.mp.yourcalendar

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import java.text.SimpleDateFormat
import java.util.*

class EventWidgetService: RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return EventRemoteViewsFactory(this.applicationContext, intent)
    }
}

class EventRemoteViewsFactory(private val context: Context, intent: Intent): RemoteViewsService.RemoteViewsFactory {

    private val appWidgetId: Int = intent.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
    )

    private var events = mutableListOf<Event>()

    // Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var ref: DatabaseReference

    override fun onCreate() {
        // Initialize FirebaseAuth instance
        auth = FirebaseAuth.getInstance()

        // Set database reference
        ref = FirebaseDatabase.getInstance().getReference("users").child(auth.uid.toString())
        // Listener for users data, runs at activity created and when data is changed

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val newList: MutableList<Event> = mutableListOf()
                for (e in snapshot.children) {
                    val event = e.getValue<Event>()
                    newList.add(event!!)
                }
                //databaseLoaded(newList)
                Log.d("DATABASE", "Users data was accessed")
                events = newList

                val appWidgetManager = context.getSystemService(AppWidgetManager::class.java)
                appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.listView)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("Database Error", "Error in DB ValueEventListener. ${error.toException()}")
            }
        })
    }


    override fun onDataSetChanged() {
        // This is triggered when you call AppWidgetManager notifyAppWidgetViewDataChanged
        // on the collection view corresponding to this factory. You can do heaving lifting in
        // here, synchronously. For example, if you need to process an image, fetch something
        // from the network, etc., it is ok to do it here, synchronously. The widget will remain
        // in its current state while work is being done here, so you don't need to worry about
        // locking up the widget.
    }

    override fun onDestroy() {
        // In onDestroy() you should tear down anything that was setup for your data source,
        // eg. cursors, connections, etc.
        events.clear()
    }

    override fun getCount(): Int {
        return events.size
    }

    override fun getViewAt(position: Int): RemoteViews {
        val event = events[position]
        Log.d("EventWidgetService", "$position: ${event.eventName}")

        val rv = RemoteViews(context.packageName, R.layout.event_widget_item)

        rv.setTextViewText(R.id.eventTextView, event.eventName)

        // String -> Date
        val dateParser = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.ROOT)
        val startDate = dateParser.parse("${event.eventStartDate} ${event.eventStartTime}")!!
        val endDate = dateParser.parse("${event.eventEndDate} ${event.eventEndTime}")!!

        // Date -> String
        val weekDayFormatter = SimpleDateFormat("E", Locale.getDefault())
        val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
        rv.setTextViewText(R.id.eventDateTextView, "${weekDayFormatter.format(startDate)} ${timeFormatter.format(startDate)} - ${timeFormatter.format(endDate)}")

        return rv
    }

    override fun getLoadingView(): RemoteViews? {
        // You can create a custom loading view (for instance when getViewAt() is slow.) If you
        // return null here, you will get the default loading view.
        return null
    }

    override fun getViewTypeCount(): Int {
        return 1
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun hasStableIds(): Boolean {
        return true
    }
}
