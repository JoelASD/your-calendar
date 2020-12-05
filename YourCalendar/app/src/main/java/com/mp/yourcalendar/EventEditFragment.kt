package com.mp.yourcalendar

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.iterator
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.mp.yourcalendar.ui.newevent.NewEventFragmentDirections
import kotlinx.android.synthetic.main.fragment_event_edit.*
import kotlinx.android.synthetic.main.new_event_fragment.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class EventEditFragment : Fragment(), DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    //args = event to be edited
    private val args: EventEditFragmentArgs by navArgs<EventEditFragmentArgs>()

    // Original event
    private lateinit var currentEvent: Event
    // Edits go to this event
    private lateinit var editedEvent: Event

    // Helper for date and time pickers
    var button: Int = 1

    // DB reference
    val database: DatabaseReference = Firebase.database.reference

    // Location
    private lateinit var geocoder: Geocoder
    private lateinit var geoList: MutableList<Address>


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_event_edit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        currentEvent = args.currentEvent
        editedEvent = args.currentEvent


        setEventDetails()
    }

    // Initialize view with current events information
    private fun setEventDetails() {
        // Name
        editEventNameEditText.setText(currentEvent.eventName)

        // Start dates and times + onClickListeners
        initDateTimeOnClick()

        // Type spinner
        initTypeSpinner()

        // Repeat spinner
        initRepeatSpinner()

        // Description
        editEventDescriptionEditText.setText(currentEvent.eventDescription)

        // Location (text)
        editEventLocationEditText.setText(currentEvent.eventLocName)
        initGeoLoc()

        // Notifications
        initNotificationViews(currentEvent.eventNotificationList)
        editEventAddNotificationButton.setOnClickListener {
            addNotificationView()
        }

        // Save button
        editEventSaveButton.setOnClickListener {
            saveEvent()
        }

    }

    // Initialize dates and times + picker dialog
    private fun initDateTimeOnClick() {
        // Start date
        editEventStartDateButton.text = currentEvent.eventStartDate
        editEventStartDateButton.setOnClickListener {
            val dParts: List<String> = editedEvent.eventStartDate.split("/")
            DatePickerDialog(requireContext(), this, dParts[2].toInt(), dParts[1].toInt()-1, dParts[0].toInt()).show() // year, month, day
            button = 1
        }

        // Start time
        editEventStartTimeButton.text = currentEvent.eventStartTime
        editEventStartTimeButton.setOnClickListener {
            val tParts: List<String> = editedEvent.eventStartTime.split(":")
            TimePickerDialog(requireContext(), this, tParts[0].toInt(), tParts[1].toInt(),true).show()
            button = 3
        }

        // End date
        editEventEndDateButton.text = currentEvent.eventEndDate
        editEventEndDateButton.setOnClickListener {
            val dParts: List<String> = editedEvent.eventEndDate.split("/")
            DatePickerDialog(requireContext(), this, dParts[2].toInt(), dParts[1].toInt()-1, dParts[0].toInt()).show()
            button = 2
        }

        // End time
        editEventEndTimeButton.text = currentEvent.eventEndTime
        editEventEndTimeButton.setOnClickListener {
            val tParts: List<String> = editedEvent.eventEndTime.split(":")
            TimePickerDialog(requireContext(), this, tParts[0].toInt(), tParts[1].toInt(),true).show()
            button = 4
        }
    }

    // Initialize type spinner
    private fun initTypeSpinner() {

        ArrayAdapter.createFromResource(
            requireActivity(),
            R.array.type_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            editEventTypeSpinner.adapter = adapter
            editEventTypeSpinner.setSelection(currentEvent.eventType)
            editEventTypeSpinner.onItemSelectedListener = object :
                AdapterView.OnItemSelectedListener{

                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    editedEvent.eventType = position
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }
    }

    // Initialize repeat spinner
    private fun initRepeatSpinner() {
        ArrayAdapter.createFromResource(
            requireActivity(),
            R.array.repeat_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            editEventRepeatSpinner.adapter = adapter
            editEventRepeatSpinner.setSelection(currentEvent.eventRepeat)
            editEventRepeatSpinner.onItemSelectedListener = object :
                AdapterView.OnItemSelectedListener{

                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    editedEvent.eventRepeat = position
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }
    }

    // Initialize already set notifications
    private fun initNotificationViews(list: MutableList<EventNotification>){
        for (notif in list) {
            // Get new row view
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
                notificationSpinner.setSelection(notif.type!!)
            }
            // Remove row button
            val removeNotificationButton: Button = notificationView.findViewById(R.id.notifRemoveButton)
            removeNotificationButton.setOnClickListener {
                //removeNotificationView(notificationView)
                editNotificationList.removeView(notificationView)
            }
            // Set new row view to page
            editNotificationList.addView(notificationView)
        }
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
            //removeNotificationView(notificationView)
            editNotificationList.removeView(notificationView)
        }
        // Set new row view to page
        editNotificationList.addView(notificationView)
    }

    // Get set notifications and validate
    private fun validateNotifications(){
        val list: MutableList<Int> = mutableListOf()

        // Loop through items in notificationList (view) and remove duplicate notifications
        for (item in editNotificationList) {
            val spinner = item.findViewById<Spinner>(R.id.notifSpinner)
            list.add(spinner.selectedItemPosition)
        }
        createNotifications(list.distinct())
    }

    // Create EventNotifications and proceed add them to newEvent
    private fun createNotifications(list: List<Int>) {
        editedEvent.eventNotificationList.clear()
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
        val dParts: List<String> = editedEvent.eventStartDate.split("/")
        val tParts: List<String> = editedEvent.eventStartTime.split(":")
        val dt: LocalDateTime = LocalDateTime.parse("${dParts[0]}/${dParts[1]}/${dParts[2]} ${tParts[0]}:${tParts[1]}", formatter)

        for (i in list) {
            // 0=at start, 1=5m, 2=10m, 3=15m, 4=30m, 5=1h, 6=2h, 7=1d, 8=1w, 9=at end
            when (i) {
                0 -> editedEvent.eventNotificationList.add(EventNotification(editedEvent.eventStartDate, editedEvent.eventStartTime, 0))
                1 -> addNotifToCollection(dt.minusMinutes(5), 1)
                2 -> addNotifToCollection(dt.minusMinutes(10), 2)
                3 -> addNotifToCollection(dt.minusMinutes(15), 3)
                4 -> addNotifToCollection(dt.minusMinutes(30), 4)
                5 -> addNotifToCollection(dt.minusHours(1), 5)
                6 -> addNotifToCollection(dt.minusHours(2), 6)
                7 -> addNotifToCollection(dt.minusDays(1), 7)
                8 -> addNotifToCollection(dt.minusWeeks(1), 8)
                9 -> editedEvent.eventNotificationList.add(EventNotification(editedEvent.eventEndDate, editedEvent.eventEndTime, 9))
            }
        }
    }

    // Helps to parse notifications actual time and date
    private fun parseAndSetNewDT(dt: LocalDateTime): List<String> {
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
        val formatted = dt.format(formatter)
        return formatted.split(" ")
    }

    // Get notifications date and time and add them to newEvents list
    private fun addNotifToCollection(dt: LocalDateTime, i: Int) {
        // Parse new date and time
        val dtParts: List<String> = parseAndSetNewDT(dt)
        // Add them to the list as new EventNotification instance
        editedEvent.eventNotificationList.add(EventNotification(dtParts[0], dtParts[1], i))
    }

    override fun onDateSet(p0: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        updateDates(button, dayOfMonth, month+1, year)
    }

    override fun onTimeSet(p0: TimePicker?, hourOfDay: Int, minute: Int) {
        updateTimes(button, hourOfDay, minute)
    }

    // Update dates and times on screen
    private fun updateDateTimeViews() {
        editEventStartDateButton.text = editedEvent.eventStartDate
        editEventStartTimeButton.text = editedEvent.eventStartTime
        editEventEndDateButton.text = editedEvent.eventEndDate
        editEventEndTimeButton.text = editedEvent.eventEndTime
    }

    // Runs after user changes start or end date
    private fun updateDates(btn: Int, day: Int, month: Int, year: Int) {
        when(btn) {
            1 -> {
                // Set new start date
                editedEvent.eventStartDate = formatDate(day, month, year)
                // End date parts
                val edp: List<String> = editedEvent.eventEndDate.split("/")
                // If new start date is after old end date -> set end date to match start date
                if (LocalDate.of(year, month, day).isAfter(LocalDate.of(edp[2].toInt(), edp[1].toInt(), edp[0].toInt()))) {
                    editedEvent.eventEndDate = formatDate(day, month, year)
                }
            }
            2 -> {
                // Set new end date
                editedEvent.eventEndDate = formatDate(day, month, year)
                // Start date parts
                val sdp: List<String> = editedEvent.eventStartDate.split("/")
                // If new end date is before start date -> set start date to match the end date
                if (LocalDate.of(year, month, day).isBefore(LocalDate.of(sdp[2].toInt(), sdp[1].toInt(), sdp[0].toInt()))) {
                    editedEvent.eventStartDate = formatDate(day, month, year)
                }
            }
        }

        // Check, if dates are equal, that start time is not after end time. Change end time if necessary
        val stp: List<String> = editedEvent.eventStartTime.split(":")
        val etp: List<String> = editedEvent.eventEndTime.split(":")
        if (editedEvent.eventStartDate == editedEvent.eventEndDate && LocalTime.of(stp[0].toInt(), stp[1].toInt()).isAfter(LocalTime.of(etp[0].toInt(), etp[1].toInt()))) {
            editedEvent.eventEndTime = currentEvent.eventStartTime
        }

        updateDateTimeViews()
    }

    // Runs after user changes start or end time
    private fun updateTimes(btn: Int, hour: Int, minute: Int) {
        val startDate = currentEvent.eventStartDate
        val endDate = currentEvent.eventEndDate
        when(btn) {
            3 -> {
                // Set new start time
                editedEvent.eventStartTime = formatTime(hour, minute)
                val etp: List<String> = editedEvent.eventEndTime.split(":")

                // If START datetime is set to be AFTER END datetime -> set END time to match start time
                if (startDate == endDate && LocalTime.of(hour, minute).isAfter(LocalTime.of(etp[0].toInt(), etp[1].toInt()))) {
                    editedEvent.eventEndTime = formatTime(hour, minute)
                }
            }
            4 -> {
                // Set new end time
                editedEvent.eventEndTime = formatTime(hour, minute)
                val stp: List<String> = editedEvent.eventStartTime.split(":")

                // If END datetime is set to be BEFORE START datetime -> set START time to match end
                if (startDate == endDate && LocalTime.of(hour, minute).isBefore(LocalTime.of(stp[0].toInt(), stp[1].toInt()))) {
                    currentEvent.eventStartTime = formatTime(hour, minute)
                }
            }
        }
        updateDateTimeViews()
    }

    // Format date string
    private fun formatDate(day: Int, month: Int, year: Int): String{
        return if (day < 10 && month < 10) "0$day/0$month/$year"
        else if (day < 10 && month > 10) "0$day/$month/$year"
        else if (day > 10 && month < 10) "$day/0$month/$year"
        else "$day/$month/$year"
    }

    // Format time string
    private fun formatTime(hour: Int, minute: Int): String{
        return if (hour < 10 && minute < 10) "0$hour:0$minute"
        else if (hour < 10 && minute > 10) "0$hour:$minute"
        else if (hour > 10 && minute < 10) "$hour:0$minute"
        else "$hour:$minute"
    }

    // Init geocoder and location
    private fun initGeoLoc() {
        // Geocoder
        geocoder = Geocoder(this.context)
        // On location change
        editEventLocationEditText.setOnKeyListener { view, keyCode, keyEvent ->
            when{
                (keyCode == KeyEvent.KEYCODE_ENTER && keyEvent.action == KeyEvent.ACTION_DOWN) -> {
                    if (editEventLocationEditText.text.trim().isNotEmpty()) {
                        getLocation(editEventLocationEditText.text.trim().toString())
                    } else  {
                        editedEvent.eventLocName = null
                        editedEvent.eventLocLatLng = null
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

        when (geoList.size) {
            0 -> {
                editedEvent.eventLocName = null
                editedEvent.eventLocLatLng = null
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

    // Select address to use
    private fun selectLocation(loc: String, latlng: String){
        editedEvent.eventLocName = loc
        editedEvent.eventLocLatLng = latlng

        editGeolocationList.removeAllViews()
        editChooseLocationTextView.visibility = View.GONE

        editEventLocationEditText.setText(loc)
        Log.d("LOCATION", "${editedEvent.eventLocName}, ${editedEvent.eventLocLatLng}")
    }

    // If multiple search result, show them in linearlayout
    private fun addLocationView(address: Address){
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

        editGeolocationList.addView(locationView)
    }

    // Finalize edited event and it to DB
    private fun saveEvent() {
        val name = editEventNameEditText.text.trim().toString()
        val desc =
                if (editEventDescriptionEditText.text.trim().isNotEmpty()) editEventDescriptionEditText.text.trim().toString()
                else null

        if (editEventNameEditText.text.trim().isNotEmpty()){
            validateNotifications()
            val newEvent = editedEvent.copy(eventName = name, eventDescription = desc, eventKey = null)

            val UID = Firebase.auth.uid
            if (UID != null){
                database.child("users")
                        .child(UID).child(currentEvent.eventKey!!)
                        .setValue(newEvent)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Event saved!", Toast.LENGTH_SHORT).show()
                            val action: NavDirections = EventEditFragmentDirections.actionEventEditToNavHome()
                            findNavController().navigate(action)
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Event couldn't be saved.. $it", Toast.LENGTH_SHORT).show()
                        }
            } else {
                Log.d("USER_ERROR", "Couldnt find user!")
            }
        } else {
            editEventNameEditText.requestFocus()
            editEventNameEditText.error = "You must set a name for the event!"
        }
    }

}