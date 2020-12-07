package com.mp.yourcalendar.ui.newevent

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.inputmethodservice.Keyboard
import android.location.Address
import android.location.Geocoder
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.iterator
import androidx.lifecycle.Observer
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.mp.yourcalendar.*
import kotlinx.android.synthetic.main.fragment_event_edit.*
import kotlinx.android.synthetic.main.new_event_fragment.*
import java.lang.reflect.Field
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class NewEventFragment : Fragment(), DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    // ViewModel
    private lateinit var newEventViewModel: NewEventViewModel

    // Helper for date and time pickers
    var button: Int = 0

    // Location
    private lateinit var geocoder: Geocoder
    private lateinit var geoList: MutableList<Address>

    //Event
    private lateinit var newEvent: Event

    // DB reference
    val database: DatabaseReference = Firebase.database.reference


    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        //newEventViewModel = ViewModelProvider(this).get(NewEventViewModel::class.java)
        return inflater.inflate(R.layout.new_event_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        newEvent = Event(eventLocName = null, eventLocLatLng = null, eventKey = null)
        super.onViewCreated(view, savedInstanceState)

        initDatesTimes()
        initTypeSpinner()
        initRepeatSpinner()
        initGeoLocation()

        newEventAddNotificationButton.setOnClickListener { addNotificationView() }

        newEventCreateButton.setOnClickListener {
            createEvent()
        }
    }

    // Initialize date and time "holders" and pickers
    private fun initDatesTimes() {
        val sdtParts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")).split(" ")
        val edtParts = LocalDateTime.now().plusHours(1).format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")).split(" ")

        newEvent.eventStartDate = sdtParts[0]
        //newEventStartDateButton.text = sdtParts[0]
        newEventStartDateButton.setOnClickListener {
            val dParts: List<String> = newEvent.eventStartDate.split("/")
            DatePickerDialog(requireContext(), this, dParts[2].toInt(), dParts[1].toInt() - 1, dParts[0].toInt()).show()
            button = 1
        }

        newEvent.eventStartTime = sdtParts[1]
        //newEventStartTimeButton.text = sdtParts[0]
        newEventStartTimeButton.setOnClickListener {
            val tParts: List<String> = newEvent.eventStartTime.split(":")
            TimePickerDialog(requireContext(), this, tParts[0].toInt(), tParts[1].toInt(), true).show()
            button = 3
        }

        newEvent.eventEndDate = edtParts[0]
        //newEventEndDateButton.text = edtParts[0]
        newEventEndDateButton.setOnClickListener {
            val dParts: List<String> = newEvent.eventEndDate.split("/")
            DatePickerDialog(requireContext(), this, dParts[2].toInt(), dParts[1].toInt() - 1, dParts[0].toInt()).show()
            button = 2
        }

        newEvent.eventEndTime = edtParts[1]
        //newEventEndTimeButton.text = edtParts[1]
        newEventEndTimeButton.setOnClickListener {
            val tParts: List<String> = newEvent.eventEndTime.split(":")
            TimePickerDialog(requireContext(), this, tParts[0].toInt(), tParts[1].toInt(), true).show()
            button = 4
        }

        updateDateTimeTexts()
    }

    // Update dates and times on screen
    private fun updateDateTimeTexts() {
        newEventStartDateButton.text = newEvent.eventStartDate
        newEventStartTimeButton.text = newEvent.eventStartTime
        newEventEndDateButton.text = newEvent.eventEndDate
        newEventEndTimeButton.text = newEvent.eventEndTime
    }

    override fun onDateSet(p0: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        updateDates(button, dayOfMonth, month + 1, year)
    }

    override fun onTimeSet(p0: TimePicker?, hourOfDay: Int, minute: Int) {
        updateTimes(button, hourOfDay, minute)
    }

    // Runs after user changes start or end date
    private fun updateDates(btn: Int, day: Int, month: Int, year: Int) {
        when (btn) {
            1 -> { // -> Start date changed by user
                // Set new start date
                newEvent.eventStartDate = formatDate(day, month, year)
                // End date parts
                val edp: List<String> = newEvent.eventEndDate.split("/")

                // If new start date is after old end date -> set end date to match start date
                if (LocalDate.of(year, month, day).isAfter(LocalDate.of(edp[2].toInt(), edp[1].toInt(), edp[0].toInt()))) {
                    newEvent.eventEndDate = formatDate(day, month, year)

                    // Now that dates are same, check that start time is not after endtime -> if yes, set endTime = startTime
                    /*val stp: List<String> = newEvent.eventStartTime.split(":")
                    val etp: List<String> = newEvent.eventEndTime.split(":")
                    if (LocalTime.of(stp[0].toInt(), stp[1].toInt()).isAfter(LocalTime.of(etp[0].toInt(), etp[1].toInt()))) {
                        newEvent.eventEndTime = newEvent.eventStartTime
                    }*/
                }
            }
            2 -> { // -> End date changed by user
                // Set new end date
                newEvent.eventEndDate = formatDate(day, month, year)
                // Start date parts
                val sdp: List<String> = newEvent.eventStartDate.split("/")

                // If new end date is before start date -> set start date to match the end date
                if (LocalDate.of(year, month, day).isBefore(LocalDate.of(sdp[2].toInt(), sdp[1].toInt(), sdp[0].toInt()))) {
                    newEvent.eventStartDate = formatDate(day, month, year)

                    // Now that dates are same, check that start time is not after endtime -> if yes, set endTime = startTime
                    /*val stp: List<String> = newEvent.eventStartTime.split(":")
                    val etp: List<String> = newEvent.eventEndTime.split(":")
                    if (LocalTime.of(stp[0].toInt(), stp[1].toInt()).isAfter(LocalTime.of(etp[0].toInt(), etp[1].toInt()))) {
                        newEvent.eventEndTime = newEvent.eventStartTime
                    }*/
                }
            }
        }

        // Check, if dates are equal, that start time is not after end time. Change end time if necessary
        val stp: List<String> = newEvent.eventStartTime.split(":")
        val etp: List<String> = newEvent.eventEndTime.split(":")
        if (newEvent.eventStartDate == newEvent.eventEndDate && LocalTime.of(stp[0].toInt(), stp[1].toInt()).isAfter(LocalTime.of(etp[0].toInt(), etp[1].toInt()))) {
            newEvent.eventEndTime = newEvent.eventStartTime
        }

        // Update times on screen
        updateDateTimeTexts()
    }

    // Runs after user changes start or end time
    private fun updateTimes(btn: Int, hour: Int, minute: Int) {
        val startDate = newEvent.eventStartDate
        val endDate = newEvent.eventEndDate
        when (btn) {
            3 -> { // Start time changed by user
                // Set new start time
                newEvent.eventStartTime = formatTime(hour, minute)
                val etp: List<String> = newEvent.eventEndTime.split(":")

                // If start datetime is set to be AFTER END datetime -> set END time to match start time
                if (startDate == endDate && LocalTime.of(hour, minute).isAfter(LocalTime.of(etp[0].toInt(), etp[1].toInt()))) {
                    newEvent.eventEndTime = formatTime(hour, minute)
                }
            }
            4 -> { // End time changed by user
                // Set new end time
                newEvent.eventEndTime = formatTime(hour, minute)
                val stp: List<String> = newEvent.eventStartTime.split(":")

                // If END datetime is set to be BEFORE START datetime -> set START time to match end
                if (startDate == endDate && LocalTime.of(hour, minute).isBefore(LocalTime.of(stp[0].toInt(), stp[1].toInt()))) {
                    newEvent.eventStartTime = formatTime(hour, minute)
                }
            }
        }
        updateDateTimeTexts()
    }

    // Format date string
    private fun formatDate(day: Int, month: Int, year: Int): String {
        return if (day < 10 && month < 10) "0$day/0$month/$year"
        else if (day < 10 && month > 10) "0$day/$month/$year"
        else if (day > 10 && month < 10) "$day/0$month/$year"
        else "$day/$month/$year"
    }

    // Format time string
    private fun formatTime(hour: Int, minute: Int): String {
        return if (hour < 10 && minute < 10) "0$hour:0$minute"
        else if (hour < 10 && minute > 10) "0$hour:$minute"
        else if (hour > 10 && minute < 10) "$hour:0$minute"
        else "$hour:$minute"
    }

    // Initialize event type spinner
    private fun initTypeSpinner() {
        ArrayAdapter.createFromResource(
                requireActivity(),
                R.array.type_array,
                android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            newEventTypeSpinner.adapter = adapter
            newEventTypeSpinner.onItemSelectedListener = object :
                    AdapterView.OnItemSelectedListener {

                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    newEvent.eventType = position
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    newEvent.eventType = 0
                }
            }
        }
    }

    // Initialize repeat selection spinner
    private fun initRepeatSpinner() {
        ArrayAdapter.createFromResource(
                requireActivity(),
                R.array.repeat_array,
                android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            newEventRepeatSpinner.adapter = adapter
            newEventRepeatSpinner.onItemSelectedListener = object :
                    AdapterView.OnItemSelectedListener {

                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    newEvent.eventRepeat = position
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    newEvent.eventRepeat = 0
                }
            }
        }
    }

    // Initialize geocoder and location selection
    private fun initGeoLocation() {
        // Geocoder
        geocoder = Geocoder(this.context)
        // Listener for enter key pressed
        newEventLocationEditText.setOnKeyListener { view, keyCode, keyEvent ->
            when {
                (keyCode == KeyEvent.KEYCODE_ENTER && keyEvent.action == KeyEvent.ACTION_DOWN) -> {
                    if (newEventLocationEditText.text.trim().isNotEmpty()) {
                        getLocation(newEventLocationEditText.text.trim().toString())
                    } else {
                        newEvent.eventLocName = null
                        newEvent.eventLocLatLng = null
                    }
                    true
                }
                else -> false
            }
        }
    }

    // Get list of locations with geocoder
    private fun getLocation(loc: String) {
        geoList = geocoder.getFromLocationName(loc, 5)
        Log.d("GEOCODER", "Results: ${geoList.size}")

        when (geoList.size) {
            0 -> {
                newEventLocationEditText.text.clear()
                newEventLocationEditText.requestFocus()
                newEventLocationEditText.error = "Location wasn't found.."
            }
            1 -> selectLocation(geoList[0].getAddressLine(0), "${geoList[0].latitude} ${geoList[0].longitude}")
            else -> {
                editChooseLocationTextView.visibility = View.VISIBLE
                for (item in geoList) {
                    addLocationView(item)
                }
            }
        }
    }

    // Select location to use
    private fun selectLocation(loc: String, latlng: String) {
        newEvent.eventLocName = loc
        newEvent.eventLocLatLng = latlng

        geolocationList.removeAllViews()
        chooseLocationTextView.visibility = View.GONE

        newEventLocationEditText.setText(loc)
    }

    // If multiple search result, show the results and let user select the one to use
    private fun addLocationView(address: Address) {
        // Get a new row view
        val locationView = layoutInflater.inflate(R.layout.row_location, null, false)

        // Rows textView
        val locationTextView: TextView = locationView.findViewById(R.id.rowLocationTextView)
        val locationString: String = address.getAddressLine(0)
        val locationLatLng = "${address.latitude} ${address.longitude}"
        locationTextView.text = address.getAddressLine(0)

        locationTextView.setOnClickListener {
            selectLocation(locationString, locationLatLng)
        }

        geolocationList.addView(locationView)
    }

    // Add new notification selection row
    private fun addNotificationView() {
        val notificationView = layoutInflater.inflate(R.layout.row_add_notification, null, false)
        // Spinner in the new row
        val notificationSpinner: Spinner = notificationView.findViewById(R.id.notifSpinner)
        ArrayAdapter.createFromResource(
                requireActivity(),
                R.array.notification_array,
                android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            notificationSpinner.adapter = adapter
        }
        // Remove row button
        val removeNotificationButton: Button = notificationView.findViewById(R.id.notifRemoveButton)
        removeNotificationButton.setOnClickListener {
            notificationList.removeView(notificationView)
        }
        // Set new row to linear layout
        notificationList.addView(notificationView)
    }

    // Get set notifications and validate
    private fun validateNotifications() {
        val list: MutableList<Int> = mutableListOf()

        // Loop through items in notificationList (view) and remove duplicate notifications
        for (item in notificationList) {
            val spinner = item.findViewById<Spinner>(R.id.notifSpinner)
            //val selectedNotificationTime = spinner.selectedItemPosition
            list.add(spinner.selectedItemPosition)
            //newEventViewModel.setNotificationDateTime(selectedNotificationTime)
        }
        createNotifications(list.distinct())
    }

    // Create EventNotifications and proceed add them to newEvent
    private fun createNotifications(list: List<Int>) {
        //editedEvent.eventNotificationList.clear()
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
        val dParts: List<String> = newEvent.eventStartDate.split("/")
        val tParts: List<String> = newEvent.eventStartTime.split(":")
        val dt: LocalDateTime = LocalDateTime.parse("${dParts[0]}/${dParts[1]}/${dParts[2]} ${tParts[0]}:${tParts[1]}", formatter)

        for (i in list) {
            // 0=at start, 1=5m, 2=10m, 3=15m, 4=30m, 5=1h, 6=2h, 7=1d, 8=1w, 9=at end
            when (i) {
                0 -> newEvent.eventNotificationList.add(EventNotification(newEvent.eventStartDate, newEvent.eventStartTime, 0))
                1 -> addNotifToCollection(dt.minusMinutes(5), 1)
                2 -> addNotifToCollection(dt.minusMinutes(10), 2)
                3 -> addNotifToCollection(dt.minusMinutes(15), 3)
                4 -> addNotifToCollection(dt.minusMinutes(30), 4)
                5 -> addNotifToCollection(dt.minusHours(1), 5)
                6 -> addNotifToCollection(dt.minusHours(2), 6)
                7 -> addNotifToCollection(dt.minusDays(1), 7)
                8 -> addNotifToCollection(dt.minusWeeks(1), 8)
                9 -> newEvent.eventNotificationList.add(EventNotification(newEvent.eventEndDate, newEvent.eventEndTime, 9))
            }
        }
    }

    // Get notifications date and time and add them to newEvents list
    private fun addNotifToCollection(dt: LocalDateTime, i: Int) {
        // Parse new date and time
        val dtParts: List<String> = parseAndSetNewDT(dt)
        // Add them to the list as new EventNotification instance
        newEvent.eventNotificationList.add(EventNotification(dtParts[0], dtParts[1], i))
    }

    // Helps to parse notifications actual time and date
    private fun parseAndSetNewDT(dt: LocalDateTime): List<String> {
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
        val formatted = dt.format(formatter)
        return formatted.split(" ")
    }

    // Finalize newEvent and proceed to save it to db
    private fun createEvent() {
        if (newEventNameEditText.text.trim().isNotEmpty()) {
            newEvent.eventName = newEventNameEditText.text.trim().toString()
            newEvent.eventDescription =
                    if (newEventDescriptionEditText.text.trim().isNotEmpty()) newEventDescriptionEditText.text.trim().toString()
                    else null

            validateNotifications()
            saveEvent()

        } else {
            newEventNameEditText.requestFocus()
            newEventNameEditText.error = "You must set a name for the event!"
        }
    }

    private fun saveEvent() {
        val UID = Firebase.auth.uid
        if (UID != null) {
            database.child("users").child(UID).push()
                    .setValue(newEvent)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Event created!", Toast.LENGTH_SHORT).show()
                        val action: NavDirections = NewEventFragmentDirections.actionNavNewEventToNavHome()
                        findNavController().navigate(action)
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Event couldn't be created.. $it", Toast.LENGTH_SHORT).show()
                    }
        } else {
            Log.d("USER_ERROR", "Couldnt find user!")
        }
    }

}