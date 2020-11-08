package com.mp.yourcalendar.ui.newevent

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.mp.yourcalendar.Event
import com.mp.yourcalendar.EventNotification
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class NewEventViewModel : ViewModel() {
    // Firebase
    val database: DatabaseReference = Firebase.database.reference
    // Event
    lateinit var event: Event
    // name
    //lateinit var eventName: String
    // Dates/Times
    lateinit var startDate: String
    lateinit var startTime: String
    lateinit var endDate: String
    lateinit var endTime: String
    // Desc
    //lateinit var eventDescription: String
    // Type
    var eventType: Int = 0
    // Repeat
    var eventRepeat: Int = 0
    // Location (class)
    //lateinit var eventLocation: String
    //lateinit var eventLocation: Location
    // Notifications
    var notificationList: MutableList<EventNotification> = mutableListOf()

    var day = 0
    var month = 0
    var year = 0
    var hour = 0
    var minute = 0

    var startDay = 0
    var startMonth = 0
    var startYear = 0
    var startHour = 0
    var startMinute = 0

    var endDay = 0
    var endMonth = 0
    var endYear = 0
    var endHour = 0
    var endMinute = 0

    // Function to initialize dates and times when fragment is opened
    fun initDateTimePicker(){
        // Initialize and format current date
        val currentDate = LocalDate.now()
        val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val dateFormatted = currentDate.format(dateFormatter)
        // Set formatted date to date variables
        startDate = dateFormatted
        endDate = dateFormatted
        // Split date and set it to needed variables (day, month, year)
        val dateParts: List<String> = dateFormatted.split("/")
        startDay = dateParts[0].toInt()
        endDay = dateParts[0].toInt()
        day = dateParts[0].toInt()
        startMonth = dateParts[1].toInt()
        endMonth = dateParts[1].toInt()
        month = dateParts[1].toInt()
        startYear = dateParts[2].toInt()
        endYear = dateParts[2].toInt()
        year = dateParts[2].toInt()

        // Initialize and format current time
        val currentTime = LocalTime.now()
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        val timeFormatted = currentTime.format(timeFormatter)
        // Split time and set them to to needed variables (hour, minute)
        val timeParts: List<String> = timeFormatted.split(":")
        startHour = timeParts[0].toInt()
        endHour = timeParts[0].toInt()
        startMinute = timeParts[1].toInt()
        endMinute = timeParts[1].toInt()
        // Set formatted times to time variables
        startTime = timeFormatted
        endTime = formatTime(endHour+1, endMinute)
    }

    // Function to update dates when new dates chosen by user
    fun updateDates(btn: Int, day: Int, month: Int, year: Int){
        // Check which date button was pressed and set new times accordingly (startDate = 1, endDate = 2)
        if (btn == 1){
            // Set new start date
            startDay = day
            startMonth = month
            startYear = year
            // format and set new startDate
            startDate = formatDate(day, month, year)

            // If new start date is after old end date -> set end date to match start date
            if (LocalDate.of(year, month, day).isAfter(LocalDate.of(endYear, endMonth, endDay))){
                endDay = day
                endMonth = month
                endYear = year
                // Format and set new endDate
                endDate = formatDate(day, month, year)

                // Now that dates are same, check that start time is not after endtime -> if yes, set endTime = startTime
                if (LocalTime.of(startHour, startMinute).isAfter(LocalTime.of(endHour,endMinute))){
                    endTime = startTime
                }
            }
        } else if (btn == 2){
            // Set new end date
            endDay = day
            endMonth = month
            endYear = year
            //format and set new endDate
            endDate = formatDate(day, month, year)

            // If new end date is before start date -> set start date to match the end date
            if (LocalDate.of(year, month, day).isBefore(LocalDate.of(startYear, startMonth, startDay))){
                startDay = day
                startMonth = month
                startYear = year
                // Format abd set new startdate
                startDate = formatDate(day, month, year)

                // Now that dates are same, check that start time is not after endtime -> if yes, set endTime = startTime
                if (LocalTime.of(startHour, startMinute).isAfter(LocalTime.of(endHour,endMinute))){
                    endTime = startTime
                }
            }
        }
    }

    // Function to update times when new times chosen by user
    fun updateTimes(btn: Int, hour: Int, minute: Int){
        // Check which time button was pressed and set new times accordingly (startTime = 3, endTime = 4)
        if (btn == 3){
            startHour = hour
            startMinute = minute
            // Format and set new startTime
            startTime = formatTime(hour, minute)

            // If START datetime is set to be AFTER END datetime -> set END time to match start
            //LocalDate.of(startYear,startMonth,startDay).isEqual(LocalDate.of(endYear,endMonth,endDay))
            if (startDate == endDate && LocalTime.of(startHour, startMinute).isAfter(LocalTime.of(endHour, endMinute))){
                endHour = hour
                endMinute = minute
                // Format and set new endTime
                endTime = formatTime(hour, minute)

            }
        } else if (btn == 4) {
            endHour = hour
            endMinute = minute
            // Format and set new endTime
            endTime = formatTime(hour, minute)

            // If END datetime is set to be BEFORE START datetime -> set START time to match end
            //startDate == endDate
            if (startDate == endDate && LocalTime.of(endHour, endMinute).isBefore(LocalTime.of(startHour, startMinute))){
                startHour = hour
                startMinute = minute
                // Format and set new startTime
                startTime = formatTime(hour, minute)

            }
        }
    }

    // Format new date and set it
    fun formatDate(day: Int, month: Int, year: Int): String{
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

        return if (day < 10 && month < 10) "0$day/0$month/$year".format(formatter)
        else if (day < 10 && month > 10) "0$day/$month/$year".format(formatter)
        else if (day > 10 && month < 10) "$day/0$month/$year".format(formatter)
        else "$day/$month/$year".format(formatter)
    }

    fun formatTime(hour: Int, minute: Int): String{
        val formatter = DateTimeFormatter.ofPattern("HH:mm")

        return if (hour < 10 && minute < 10) "0$hour:0$minute".format(formatter)
        else if (hour < 10 && minute > 10) "0$hour:$minute".format(formatter)
        else if (hour > 10 && minute < 10) "$hour:0$minute".format(formatter)
        else "$hour:$minute".format(formatter)

    }

    fun saveEvent(name: String, desc: String, loc: String){
        //val notif = EventNotification("title", "description", "1.1.1111", "10:10")
        //val notif2 = EventNotification("title2", "description2", "2.2.2222", "20:20")
        //var lista = mutableListOf<EventNotification>(notif, notif2)
        //Log.d("NOTIFICATION", "Notification made: $notif")
        event = Event(name, startDate, startTime, endDate, endTime, desc, eventType, eventRepeat, loc)
        //Log.d("EVENT", "${event.eventName}, ${event.eventStartDate}, ${event.eventStartTime}, ${event.eventEndDate}, ${event.eventEndTime}, ${event.eventType}, ${event.eventRepeat}, ${event.eventLocName}, ${event.eventNotificationList.elementAt(0)}, ${event.eventNotificationList.elementAt(1)}")
        val userUID = Firebase.auth.uid
        if (userUID != null){
            database.child("users").child(userUID).push().setValue(event)
        }
    }

}