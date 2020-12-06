package com.mp.yourcalendar.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.viewpager2.widget.ViewPager2
import com.mp.yourcalendar.Event
import com.mp.yourcalendar.R
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

    private lateinit var homeViewModel: HomeViewModel

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
        //homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
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
            //val chosenDate = "${dateParts2[2]}/${dateParts2[2]}/${dateParts2[0]}"
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
                //Log.d("DATES", "${event.eventStartDate} - $date")
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

        val adapter = EventSlidePagerAdapter(requireActivity(), properList)
        viewPager.adapter = adapter
        viewPager.currentItem = Int.MAX_VALUE / 2
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                // When we slide left or right, we update the selected date on the calendar
                val calendar = Calendar.getInstance()
                calendar.time = adapter.date
                calendar.add(Calendar.DATE, position)
                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH) + 1
                val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
                calendarView.selectedDate = CalendarDay.from(year, month, dayOfMonth)
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
            Log.d("OBSERVER", "data changed, size: ${newProperList.size}")
            if (properList != newProperList) {
                properList.clear()
                properList = newProperList

                eventList.clear()
                eventList.addAll(viewModel.eventList)
                Log.d("OBSERVER", "home eventlist size: ${eventList.size}")
            } else {
                Log.d("OBSERVER", "Lists are equal")
            }
            initCalendar()
        })

    }
    //check, if Calendar is changed
    fun observeCalendarChange() {
        viewModel.calendarType.observe(viewLifecycleOwner, Observer { newViewInt ->
            when (newViewInt) {
                1 -> calendarView.state().edit().setCalendarDisplayMode(CalendarMode.MONTHS).commit()
                2 -> calendarView.state().edit().setCalendarDisplayMode(CalendarMode.WEEKS).commit()
            }
        })
    }

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

    /*override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        // TODO: Use the ViewModel
    }*/

    /*fun createRepeatEvents(events: MutableList<Event>) : MutableList<Event> { //, selection: Int

        val list: MutableList<Event> = mutableListOf()

        for (e in events) {
            //val sParts: List<String> = e.eventStartDate.split("/")
            //val eParts: List<String> = e.eventEndDate.split("/")
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            //val sDate: LocalDate = LocalDate.parse("${sParts[0]}/${sParts[1]}/${sParts[2]}", formatter)
            //val eDate: LocalDate = LocalDate.parse("${eParts[0]}/${eParts[1]}/${eParts[2]}", formatter)
            val sDate: LocalDate = LocalDate.parse("${e.eventStartDate}", formatter)
            val eDate: LocalDate = LocalDate.parse("${e.eventEndDate}", formatter)
            Log.d("datetime", "$sDate, $eDate")
            // 0=no repeat, 1=everyday, 2=every weekday, 3=weekly, 4=monthly, 5=yearly
            when(e.eventRepeat) {
                0 -> {
                    Log.d("REPEAT", "NO Repeat found")
                    list.add(e)
                }
                1 -> {
                    Log.d("REPEAT", "NO Repeat found")
                    list.add(e)
                }
                2 -> {
                    Log.d("REPEAT", "NO Repeat found")
                    list.add(e)
                }
                3 -> {
                    //Plus week for startDate and endDate
                    val newSD: LocalDate = sDate.plusWeeks(1)
                    val newED: LocalDate = eDate.plusWeeks(1)
                    // Get new date and time
                    val newStartDate: String = parseAndSetNewDT(newSD)
                    val newEndDate: String = parseAndSetNewDT(newED)
                    //Log.d("TIMES", "$newStartDate $newEndDate / ${e.eventStartDate} ${e.eventEndDate}")
                    // Create a new Event with new startDate and Add it to the list
                    val newE = e.copy(eventStartDate = newStartDate, eventEndDate = newEndDate)
                    list.add(newE)
                    list.add(e)
                }
                4 -> {
                    // Plus month for startDate and endDate
                    val newSD: LocalDate = sDate.plusMonths(1)
                    val newED: LocalDate = eDate.plusMonths(1)
                    // Get new date and time
                    val newStartDate: String = parseAndSetNewDT(newSD)
                    val newEndDate: String = parseAndSetNewDT(newED)
                    //Log.d("TIMES", "$newStartDate $newEndDate / ${e.eventStartDate} ${e.eventEndDate}")
                    // Add them to the list as new EventNotification instances
                    val newE = e.copy(eventStartDate = newStartDate, eventEndDate = newEndDate)
                    list.add(newE)
                    list.add(e)
                }
                5 -> {
                    // Plus year for startDate and endDate
                    val newSD: LocalDate = sDate.plusYears(1)
                    val newED: LocalDate = eDate.plusYears(1)
                    // Get new date and time
                    val newStartDate: String = parseAndSetNewDT(newSD)
                    val newEndDate: String = parseAndSetNewDT(newED)
                    //Log.d("TIMES", "$newStartDate $newEndDate / ${e.eventStartDate} ${e.eventEndDate}")
                    // Add them to the list as new EventNotification instances
                    val newE = e.copy(eventStartDate = newStartDate, eventEndDate = newEndDate)
                    list.add(newE)
                    list.add(e)
                }
            }
        }
        return list
    }*/



    // Minus the amount time from start time/date
    fun plus(amount: TemporalAmount, dt: LocalDate): LocalDate {
        return dt+amount
    }


    // Parse new date and time for notification
    //TODO: See if actually needed??
    private fun parseAndSetNewDT(dt: LocalDate): String {
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        return dt.format(formatter).toString()
    }
}