package com.mp.yourcalendar.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.mp.yourcalendar.Event
import com.mp.yourcalendar.MainActivity
import com.mp.yourcalendar.R
import com.mp.yourcalendar.ui.newevent.NewEventFragmentDirections
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.CalendarMode
import kotlinx.android.synthetic.main.home_fragment.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAmount
import java.util.*


class HomeFragment : Fragment() {

    // Uses viewModel owned by MainActivity, more info in activityViewModel.kt
    private val viewModel by activityViewModels<activityViewModel>()

    companion object {
        var eventList: MutableList<Event> = mutableListOf()
        var properList: MutableList<Event> = mutableListOf()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.home_fragment, container, false)

        // Setup observer for eventList when view is being created
        observeDatabaseChange()
        observeCalendarChange()

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Sets date selected to current day after view is created
        calendarView.selectedDate = CalendarDay.today()
        dateChecker.text = DateFormat.getDateInstance().format(Date())

        // Calendar click handling, to show specific dates events
        calendarView.setOnDateChangedListener { widget, date, selected ->
            val calendarCalendar = Calendar.getInstance()
            calendarCalendar.set(date.year, date.month, date.day)
            dateChecker.text = DateFormat.getDateInstance().format(calendarCalendar.time)

            /*val d: String = date.toString()
            val dateParts: List<String> = d.split("{" ,"}")
            val dateParts2: List<String> = dateParts[1].split("-")
            val chosenDate = formatDate(dateParts2[2].toInt(), dateParts2[1].toInt(), dateParts2[0].toInt())
            //getDailyEvents(chosenDate)
            dateChecker.text = chosenDate*/

            // We calculate the difference between today and the chosen date
            val calendar = Calendar.getInstance()
            calendar.time = Date()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH) + 1
            val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
            val startDate = LocalDate.of(year, month, dayOfMonth)
            val calendarDate = LocalDate.of(date.year, date.month, date.day)
            val offset = ChronoUnit.DAYS.between(startDate, calendarDate).toInt()

            //we update the position on the viewpager
            //we Update the slider when we press on a date on the calendar
            viewPager.currentItem = Int.MAX_VALUE / 2 + offset
        }

        // FAB listener
        fab.setOnClickListener {
            val action: NavDirections = HomeFragmentDirections.actionNavHomeToNavNewEvent()
            findNavController().navigate(action)
        }
    }

    // Gets list of events and runs EventsAdapter with them to show in recyclerView
    /*fun setupRecyclerView(events: MutableList<Event>){
        recyclerView.layoutManager = LinearLayoutManager(this.context)
        recyclerView.adapter = EventsAdapter(this, events, eventList)
    }*/

    /*
    *   Gets date (from initCalendar() or calendarView being pressed)
    *   Finds events with that date and runs setupRecyclerView() to
    *   set those events into recyclerView
     */
    /*fun getDailyEvents(date: String){
        val list: MutableList<Event> = mutableListOf()
        for (event in properList){
            if (event.eventStartDate == date) {
                list.add(event)
            }
        }
        setupRecyclerView(list)
    }*/

    // Gets current date and runs function to set events from that date to recyclerView

    //Viewpager Setup
    fun initCalendar(){
        // Sets markers to calendar days when there is events
        getDaysWithEvents()

        // Gets current date and runs function to set events from that date to recyclerView
        //val dateParts: List<String> = LocalDate.now().toString().split("-")
        //getDailyEvents("${dateParts[2]}/${dateParts[1]}/${dateParts[0]}")

        val adapter = EventSlidePagerAdapter(requireActivity(), properList, eventList)
        viewPager.adapter = adapter
        viewPager.currentItem = Int.MAX_VALUE / 2
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                // When we slide left or right, we update the selected date on the calendar
                val calendar = Calendar.getInstance()
                calendar.time = adapter.date
                calendar.add(Calendar.DATE, position)
                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH)
                val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
                calendarView.selectedDate = CalendarDay.from(year, month+1, dayOfMonth)
                val calendarCalendar = Calendar.getInstance()
                calendarCalendar.set(year, month, dayOfMonth)
                dateChecker.text = DateFormat.getDateInstance().format(calendarCalendar.time)
            }
        })
    }

    /*
    *   Changed a bit
    *
    *   Observes activityViewModels eventList for changes. When changes happen, updates own eventList.
    *   But in the case of this app currently, only runs when HomeFragment is being opened. Since data in database
    *   is changed only during user being in different fragments (eg. newEventFragment or eventDetailFragment).
    *
    *   That's why initCalendar() is ran at the end
     */
    fun observeDatabaseChange() {
        viewModel.properList.observe(viewLifecycleOwner, Observer { newProperList ->
            if (properList != newProperList) {
                properList.clear()
                properList = newProperList

                // Maybe not needed?
                eventList.clear()
                eventList.addAll(viewModel.eventList)
            }
            initCalendar()
        })

    }

    // Switches calendar view mode
    fun observeCalendarChange() {
        viewModel.calendarType.observe(viewLifecycleOwner, Observer { newViewInt ->
            when (newViewInt) {
                1 -> calendarView.state().edit().setCalendarDisplayMode(CalendarMode.MONTHS).commit()
                2 -> calendarView.state().edit().setCalendarDisplayMode(CalendarMode.WEEKS).commit()
            }
        })
    }

    // Iterates through properList to get dates, marks date that have events on calendar
    fun getDaysWithEvents() {
        val list: MutableCollection<CalendarDay> = mutableListOf()
        for (event in properList) {
            val dateParts: List<String> = event.eventStartDate.split("/")
            list.add(CalendarDay.from(dateParts[2].toInt(), dateParts[1].toInt(), dateParts[0].toInt()))
        }
        val eventDecorator = EventDecorator(0xFFFF0000.toInt(), list)
        calendarView.addDecorator(eventDecorator)
    }

    // Format new date
    fun formatDate(day: Int, month: Int, year: Int): String{
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

        return if (day < 10 && month < 10) "0$day/0$month/$year".format(formatter)
        else if (day < 10 && month > 10) "0$day/$month/$year".format(formatter)
        else if (day > 10 && month < 10) "$day/0$month/$year".format(formatter)
        else "$day/$month/$year".format(formatter)
    }
}