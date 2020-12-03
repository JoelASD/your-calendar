package com.mp.yourcalendar

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.util.Log
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import java.text.SimpleDateFormat
import java.time.LocalTime
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

        //checking if users event is active for the day
        val weekday = SimpleDateFormat("dd/MM/yyyy")
        val justADateString = weekday.format(Date())
        //checking daytime
        val daytime = SimpleDateFormat("HH:mm")

        events.clear()
        // Listener for users data, runs at activity created and when data is changed
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val newList: MutableList<Event> = mutableListOf()
                for (e in snapshot.children) {
                    val event = e.getValue<Event>()
                    //checking for the Event, if for today
                    //idea behind the widget was, that it shows only the events, which are planned for today
                    if (event!!.eventStartDate == justADateString) {
                        newList.add(event!!)
                    }
                }
                putInOrder(newList)
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

        if (position % 2 == 1) {
            rv.setInt(R.id.linearLayoutWidgetList, "setBackgroundColor", Color.WHITE)
            rv.setInt(R.id.eventTextView, "setTextColor", Color.DKGRAY)
            rv.setInt(R.id.eventDateTextView, "setTextColor", Color.DKGRAY)
        } else {
            rv.setInt(R.id.linearLayoutWidgetList, "setBackgroundColor", Color.LTGRAY)
            rv.setInt(R.id.eventTextView, "setTextColor", Color.WHITE)
            rv.setInt(R.id.eventDateTextView, "setTextColor", Color.WHITE)
        }
        //Event type coloring
        when (event.eventType) {
            0 -> rv.setInt(R.id.colorTextView, "setBackgroundResource", R.drawable.box_blue)
            1 -> rv.setInt(R.id.colorTextView, "setBackgroundResource", R.drawable.box_green)
            2 -> rv.setInt(R.id.colorTextView, "setBackgroundResource", R.drawable.box_red)
            3 -> rv.setInt(R.id.colorTextView, "setBackgroundResource", R.drawable.box_yellow)
            4 -> rv.setInt(R.id.colorTextView, "setBackgroundResource", R.drawable.box_pink)
            5 -> rv.setInt(R.id.colorTextView, "setBackgroundResource", R.drawable.box_purple)
            6 -> rv.setInt(R.id.colorTextView, "setBackgroundResource", R.drawable.box_brown)
            else -> rv.setInt(R.id.colorTextView, "setBackgroundColor", Color.BLACK)
        }

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

    //To compare Time of the event and change the order in the List
    fun putInOrder(list: MutableList<Event>) {
        val helperList: MutableList<Event> = list
        for(pos in 1 until list.size) {
            val sTimePart: List<String> = list[pos].eventStartTime.split(":")
            for(item in helperList) {
                val sTimePart2: List<String> = item.eventStartTime.split(":")
                if(LocalTime.of(sTimePart[0].toInt(), sTimePart[1].toInt()).isBefore(LocalTime.of(sTimePart2[0].toInt(), sTimePart2[1].toInt()))) {
                    Collections.swap(helperList, pos - 1, pos )
                    break
                }
            }
        }
    }

}
