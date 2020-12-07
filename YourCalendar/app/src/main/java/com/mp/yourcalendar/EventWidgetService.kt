package com.mp.yourcalendar

import android.app.PendingIntent
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
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
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
    private var todayEventsList = mutableListOf<Event>()

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



        // Listener for users data, runs at activity created and when data is changed
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                events.clear()
                todayEventsList.clear()
                val newList: MutableList<Event> = mutableListOf()
                for (e in snapshot.children) {
                    val event = e.getValue<Event>()
                    //checking for the Event, if for today
                    //idea behind the widget was, that it shows only the events, which are planned for today
                        newList.add(event!!)
                }

                //This used to add the the daily events and the repeating events
                val helperList: MutableList<Event> = mutableListOf()
                helperList.addAll(newList)
                helperList.addAll(multipleDayEvents(newList))
                putInOrder(addRepeats(helperList), justADateString)
                events = todayEventsList
                helperList.clear()
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

      /**  holder.startDateTimeText.text =
            if (e.eventStartDate != e.eventEndDate) "${e.eventStartTime} - 23:59"
            else if (e.eventStartTime == "00:00" && e.eventEndTime == "23:59") "Whole day"
            else "${e.eventStartTime} - ${e.eventEndTime}"
**/
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
    fun putInOrder(list: MutableList<Event>, date: String) {
        Log.d("putOrder", "in putOrder")
        for(e in list) {
            Log.d("ListWidget", "${e.eventStartDate}, ${date}")
            if(e.eventStartDate == date) {
                Log.d("ListWidget2", "add todayEvent")
                todayEventsList.add(e)
            }
        }
        for(pos in 1 until todayEventsList.size) {
            Log.d("ListWidget3", "in for lopp todaysEvent")
            val sTimePart: List<String> = list[pos].eventStartTime.split(":")
            for(item in todayEventsList) {
                val sTimePart2: List<String> = item.eventStartTime.split(":")
                if(LocalTime.of(sTimePart[0].toInt(), sTimePart[1].toInt()).isBefore(LocalTime.of(sTimePart2[0].toInt(), sTimePart2[1].toInt()))) {
                    Collections.swap(todayEventsList, pos - 1, pos )
                    break
                }
            }
        }
    }

    /*
    *
    * Loops through list and creates "new events" with proper dates and times
    * according to multi day events lengths (start date - end date).
    * These created events are only used in the home fragment calendar.
    * Never saved into DB.
    *
    * Returns list with added events for multi day entries
    *
     */
    private fun multipleDayEvents(list: MutableList<Event>): MutableList<Event> {
        val modifiedList: MutableList<Event> = mutableListOf()
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

        for(e in list){
            // If event goes into multiple days
            if(e.eventStartDate != e.eventEndDate) {
                // Gets time between start and end
                var sDate: LocalDate = LocalDate.parse(e.eventStartDate, formatter)
                var eDate: LocalDate = LocalDate.parse(e.eventEndDate, formatter)
                val period = sDate.until(eDate)

                // helpers
                val newStartTime = "00:00"
                val newEndTime = "23:59"

                // Create events according to days between start and end
                for(i in 1 until period.days){
                    // Next day
                    var newSD: LocalDate = sDate.plusDays(1)
                    // Format
                    val newStartDate: String = parseAndSetNewDT(newSD)
                    // Save date for next loop
                    sDate = newSD
                    // Create new event with new start and end datetimes, dates are equal
                    val newE = e.copy(eventStartDate = newStartDate, eventStartTime = newStartTime, eventEndDate = newStartDate, eventEndTime = newEndTime)
                    modifiedList.add(newE)
                }
                // Add final day event
                var newSD: LocalDate = sDate.plusDays(1)
                val newStartDate: String = parseAndSetNewDT(newSD)
                val finalDay = e.copy(eventStartDate = newStartDate, eventStartTime = newStartTime, eventEndDate = newStartDate)
                modifiedList.add(finalDay)
            }
        }
        return modifiedList
    }

    /*
    *
    * Gets list with multi day events already added. Looks for events
    * with that have repeating set. Creates repeat events accordingly.
    * These created events are only used in the home fragment calendar.
    * Never saved into DB.
    * Returns list with added events for repeat entries
    *
     */
    private fun addRepeats(originalEvents: MutableList<Event>): MutableList<Event>{
        val listWithRepeats: MutableList<Event> = mutableListOf()
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

        for (e in originalEvents) {
            val sDate: LocalDate = LocalDate.parse(e.eventStartDate, formatter)
            val eDate: LocalDate = LocalDate.parse(e.eventEndDate, formatter)
            // 0=no repeat, 1=weekly, 2=monthly, 3=yearly
            when(e.eventRepeat) {
                0 -> {
                    listWithRepeats.add(e)
                }
                1 -> {
                    //Plus week for startDate and endDate (13 times the event, for ~3 months)
                    var newSD: LocalDate = sDate.plusWeeks(1)
                    var newED: LocalDate = eDate.plusWeeks(1)
                    for (i in 1..12) {
                        // Get new dates
                        val newStartDate: String = parseAndSetNewDT(newSD)
                        val newEndDate: String = parseAndSetNewDT(newED)
                        // Save new dates to use in next loop cycle
                        newSD = newSD.plusWeeks(1)
                        newED = newED.plusWeeks(1)
                        // Create a new Event with new startDate and Add it to the list
                        val newE = e.copy(eventStartDate = newStartDate, eventEndDate = newEndDate)
                        listWithRepeats.add(newE)
                    }
                    listWithRepeats.add(e)
                }
                2 -> {
                    // Plus month for startDate and endDate (12 times the event, for a year)
                    var newSD: LocalDate = sDate.plusMonths(1)
                    var newED: LocalDate = eDate.plusMonths(1)
                    for (i in 1..11) {
                        // Get new dates
                        val newStartDate: String = parseAndSetNewDT(newSD)
                        val newEndDate: String = parseAndSetNewDT(newED)
                        // Save new dates to use in next loop cycle
                        newSD = newSD.plusMonths(1)
                        newED = newED.plusMonths(1)
                        // Add them to the list as new EventNotification instances
                        val newE = e.copy(eventStartDate = newStartDate, eventEndDate = newEndDate)
                        listWithRepeats.add(newE)
                    }
                    listWithRepeats.add(e)
                }
                3 -> {
                    // Plus year for startDate and endDate (10 times the event, for 10 years)
                    var newSD: LocalDate = sDate.plusYears(1)
                    var newED: LocalDate = eDate.plusYears(1)
                    for (i in 1..9) {
                        // Get new dates
                        val newStartDate: String = parseAndSetNewDT(newSD)
                        val newEndDate: String = parseAndSetNewDT(newED)
                        // Save new dates to use in next loop cycle
                        newSD = newSD.plusYears(1)
                        newED = newSD.plusYears(1)
                        // Add them to the list as new EventNotification instances
                        val newE = e.copy(eventStartDate = newStartDate, eventEndDate = newEndDate)
                        listWithRepeats.add(newE)
                    }
                    listWithRepeats.add(e)
                }
            }
        }
        return listWithRepeats
    }

    // Parse new date and time for notification
    private fun parseAndSetNewDT(dt: LocalDate): String {
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        return dt.format(formatter).toString()
    }

}
