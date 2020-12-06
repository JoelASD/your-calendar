package com.mp.yourcalendar.ui.home

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import com.mp.yourcalendar.Event
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import javax.xml.datatype.DatatypeConstants.DAYS

/*
*
*   This viewModel is owned by main activity.
*   Contains events and calendar mode liveData that home fragment uses.
*
 */

class activityViewModel(application: Application): AndroidViewModel(application) {

    // List of events from database, no repeats or multi days created
    // Used when needed the original created event
    val eventList: MutableList<Event> = mutableListOf()

    // List of events + created repeat events + created multi day events
    // Used in calendar
    val properList = MutableLiveData<MutableList<Event>>()

    // Helps creating properlist, TODO: change to run all in order from functions?
    private val helperList: MutableList<Event> = mutableListOf()

    // Week / month view
    val calendarType = MutableLiveData<Int>()

    // Gets data from DB, set it to lists
    fun setEventList(eList: MutableList<Event>, kList: MutableList<String>) {
        eventList.clear()
        eventList.addAll(eList)

        helperList.clear()
        helperList.addAll(eList)
        helperList.addAll(multipleDayEvents(eList))
        properList.value = addRepeats(helperList)
    }

    // When calendar view is wanted to be changed
    fun changeCalendarView(statusInt : Int) { calendarType.value = statusInt }

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
            // 0=no repeat, 1=everyday, 2=every weekday, 3=weekly, 4=monthly, 5=yearly
            when(e.eventRepeat) {
                0 -> {
                    listWithRepeats.add(e)
                }
                1 -> {
                    listWithRepeats.add(e)
                }
                2 -> {
                    listWithRepeats.add(e)
                }
                3 -> {
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
                4 -> {
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
                5 -> {
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
