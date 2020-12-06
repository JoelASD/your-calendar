package com.mp.yourcalendar.ui.home

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.mp.yourcalendar.Event
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

/**
 * The adapter is responsible for the viewpager
 */
class EventSlidePagerAdapter(
    fa: FragmentActivity,
    val events: MutableList<Event>,
    val originalEvents: MutableList<Event>
) : FragmentStateAdapter(fa) {

    //We use this date, to get the current date
    //when we get a position, it is half of the int.max
    //this is the reason, why we - Int max_Value / 2 to get to position 0
    val date: Date get() {
        val calendar = Calendar.getInstance()
        calendar.time = Date()
        calendar.add(Calendar.DATE, -(Int.MAX_VALUE / 2))
        return calendar.time
    }

    override fun getItemCount(): Int = Int.MAX_VALUE

    override fun createFragment(position: Int): Fragment {
        //get the date according to the viewpager position
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.add(Calendar.DATE, position)

        // filter all events by the selected date
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.ROOT)
        val dayEvents = ArrayList(events.filter { it.eventStartDate == dateFormat.format(calendar.time) })
        //pass the filtered events to the eventSlidePageFragment
        val orEvents = ArrayList(originalEvents)
        val bundle = Bundle()
        bundle.putParcelableArrayList("events", dayEvents)
        bundle.putParcelableArrayList("originalEvents", orEvents)
        val fragment = EventSlidePageFragment()
        fragment.arguments = bundle
        return fragment
    }
}
