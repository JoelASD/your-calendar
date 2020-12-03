package com.mp.yourcalendar.ui.newevent

import android.app.DatePickerDialog
import android.app.TimePickerDialog
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
import androidx.core.view.get
import androidx.core.view.isVisible
import androidx.core.view.iterator
import androidx.lifecycle.Observer
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.mp.yourcalendar.Event
import com.mp.yourcalendar.R
import kotlinx.android.synthetic.main.event_appwidget.*
import kotlinx.android.synthetic.main.new_event_fragment.*
import java.text.DateFormat
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAmount
import java.util.*

class NewEventFragment : Fragment(), DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    // ViewModel
    private lateinit var newEventViewModel: NewEventViewModel
    // Button helper
    var button: Int = 0
    // Location
    private lateinit var geocoder: Geocoder
    private lateinit var geoList: MutableList<Address>
    //Event
    private lateinit var newEvent: Event
    //Firebase
    //private lateinit var database: DatabaseReference



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        newEventViewModel = ViewModelProvider(this).get(NewEventViewModel::class.java)
        return inflater.inflate(R.layout.new_event_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        newEventViewModel.initDateTimePicker()

        // Start date button
        newEventStartDateButton.text = "${newEventViewModel.startDate}"
        newEventStartDateButton.setOnClickListener {
            DatePickerDialog(requireActivity(),this, newEventViewModel.startYear, newEventViewModel.startMonth-1, newEventViewModel.startDay).show()
            button = 1
        }

        // Start time button
        newEventStartTimeButton.text = "${newEventViewModel.startTime}"
        newEventStartTimeButton.setOnClickListener {
            TimePickerDialog(requireActivity(), this, newEventViewModel.startHour, newEventViewModel.startMinute, true).show()
            button = 3
        }

        // End date button
        newEventEndDateButton.text = "${newEventViewModel.endDate}"
        newEventEndDateButton.setOnClickListener {
            DatePickerDialog(requireActivity(), this, newEventViewModel.endYear, newEventViewModel.endMonth-1, newEventViewModel.endDay).show()
            button = 2
        }

        // End time button
        newEventEndTimeButton.text = "${newEventViewModel.endTime}"
        newEventEndTimeButton.setOnClickListener {
            TimePickerDialog(requireActivity(), this, newEventViewModel.endHour, newEventViewModel.endMinute, true).show()
            button = 4
        }

        // Event type spinner (adapter)
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
                AdapterView.OnItemSelectedListener{

                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    // Set the chosen items id to eventType
                    newEventViewModel.eventType = newEventTypeSpinner.adapter.getItemId(position).toInt()
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // Use the first item if nothing is selected by user
                    newEventViewModel.eventType = 0
                }
            }
        }

        // Repeat spinner (adapter)
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
                AdapterView.OnItemSelectedListener{

                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    // Set the chosen items id to eventRepeat variable
                    newEventViewModel.eventRepeat = newEventRepeatSpinner.adapter.getItemId(position).toInt()
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // Use first option if nothing selected
                    newEventViewModel.eventRepeat = 0
                }
            }
        }

        // Geocoder
        geocoder = Geocoder(this.context)

        // Location search edit text
        newEventLocationEditText.setOnKeyListener { v, keyCode, event ->
            when{
                (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN) -> {
                    if(newEventLocationEditText.text.trim().isNotEmpty()){
                        getLocation(newEventLocationEditText.text.trim().toString())
                    } else {
                        newEventViewModel.eventLocation = null
                        newEventViewModel.eventLatLng = null
                    }
                    true
                }
                else -> false
            }
        }

        // Location search button (removed for now)
        /*searchLocButton.setOnClickListener {
            if(newEventLocationEditText.text.trim().isNotEmpty()){
                getLocation(newEventLocationEditText.text.trim().toString())
            } else {
                newEventLocationEditText.requestFocus()
                newEventLocationEditText.error = "Enter an address and/or city"
            }
        }*/

        // Add notifications button
        newEventAddNotificationButton.setOnClickListener {
            addNotificationView()
        }

        // Add event button (create event and save it to db)
        newEventCreateButton.setOnClickListener {
            // Make sure also event name is given before saving new event to DB
            if (newEventNameEditText.text.trim().isNotEmpty()){
                validateAndCreateNotifications()

                newEvent =
                    if(newEventDescriptionEditText.text.trim().toString().isEmpty()) newEventViewModel.createEvent(newEventNameEditText.text.trim().toString(), null)
                    else newEventViewModel.createEvent(newEventNameEditText.text.trim().toString(), newEventDescriptionEditText.text.trim().toString())

                newEventViewModel.saveEvent(newEvent)
                val action: NavDirections = NewEventFragmentDirections.actionNavNewEventToEventDetail(newEvent)
                findNavController().navigate(action)
                //findNavController().navigate(R.id.action_nav_new_event_to_event_detail)
            } else {
                newEventNameEditText.requestFocus()
                newEventNameEditText.error = "You must set a name for the event!"
            }
        }
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        newEventViewModel.day = dayOfMonth
        newEventViewModel.month = month+1
        newEventViewModel.year = year

        runUpdate(button)
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        newEventViewModel.hour = hourOfDay
        newEventViewModel.minute = minute
        runUpdate(button)
    }

    // To see what to do after new date or time is set
    fun runUpdate(btn: Int){
        when (btn){
            1 -> {
                // Run updateDates function
                newEventViewModel.updateDates(1, newEventViewModel.day, newEventViewModel.month, newEventViewModel.year)
                // Set new dates to view/buttons
                newEventStartDateButton.text = newEventViewModel.startDate
                newEventEndDateButton.text = newEventViewModel.endDate
                newEventStartTimeButton.text = newEventViewModel.startTime
                newEventEndTimeButton.text = newEventViewModel.endTime
            }
            2 -> {
                newEventViewModel.updateDates(2, newEventViewModel.day, newEventViewModel.month, newEventViewModel.year)
                newEventStartDateButton.text = newEventViewModel.startDate
                newEventEndDateButton.text = newEventViewModel.endDate
                newEventStartTimeButton.text = newEventViewModel.startTime
                newEventEndTimeButton.text = newEventViewModel.endTime
            }
            3 -> {
                // Run updateTimes function
                newEventViewModel.updateTimes(3, newEventViewModel.hour, newEventViewModel.minute)
                // Set new times to view/buttons
                newEventStartTimeButton.text = newEventViewModel.startTime
                newEventEndTimeButton.text = newEventViewModel.endTime
                newEventStartDateButton.text = newEventViewModel.startDate
                newEventEndDateButton.text = newEventViewModel.endDate
            }
            4 -> {
                newEventViewModel.updateTimes(4, newEventViewModel.hour, newEventViewModel.minute)
                newEventStartTimeButton.text = newEventViewModel.startTime
                newEventEndTimeButton.text = newEventViewModel.endTime
                newEventStartDateButton.text = newEventViewModel.startDate
                newEventEndDateButton.text = newEventViewModel.endDate
            }
        }
    }

    fun addNotificationView(){
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
        }
        // Remove row button
        val removeNotificationButton: Button = notificationView.findViewById(R.id.notifRemoveButton)
        removeNotificationButton.setOnClickListener {
            removeNotificationView(notificationView)
        }
        // Set new row view to page
        notificationList.addView(notificationView)
        //notificationLayoutList.addView(notificationView)
    }

    fun removeNotificationView(view: View){
        //notificationLayoutList.removeView(view)
        notificationList.removeView(view)
    }

    //
    fun validateAndCreateNotifications(){
        // Clear notification list in case there is something left
        newEventViewModel.notificationList.clear()

        //TODO: make sure no duplicate notifications
        // Go through notificationLayoutList and make new notification
        for (item in notificationList) {
            val spinner = item.findViewById<Spinner>(R.id.notifSpinner)
            val selectedNotificationTime = spinner.selectedItemPosition
            newEventViewModel.setNotificationDateTime(selectedNotificationTime)
        }
    }

    // Search given location with geocoder
    fun getLocation(loc: String){
        geoList = geocoder.getFromLocationName(loc, 5)



        when(geoList.size){
            0 -> {
                newEventViewModel.eventLocation = null
                newEventViewModel.eventLatLng = null
            }
            1 -> selectLocation(geoList[0].getAddressLine(0), "${geoList[0].latitude} ${geoList[0].longitude}")
            else -> {
                chooseLocationTextView.visibility = View.VISIBLE
                for(item in geoList){
                    addLocationView(item)
                }
            }
        }

        //if only 1 result -> select it automatically
        /*if(geoList.size == 1){
            selectLocation(geoList[0].getAddressLine(0), "${geoList[0].latitude} ${geoList[0].longitude}")
        } else {
            for(item in geoList){
                addLocationView(item)
            }
        }*/
    }

    // If multiple search result, show them in linearlayout
    fun addLocationView(address: Address){
        // Get a new row view
        val locationView = layoutInflater.inflate(R.layout.row_location, null, false)

        // Rows textView
        val locationTextView: TextView = locationView.findViewById(R.id.rowLocationTextView)
        val locationString: String = address.getAddressLine(0)
        val locationLatLng: String = "${address.latitude} ${address.longitude}"
        locationTextView.text = address.getAddressLine(0)

        locationTextView.setOnClickListener {
            selectLocation(locationString, locationLatLng)
        }

        geolocationList.addView(locationView)
    }

    // Select address to use
    fun selectLocation(loc: String, latlng: String){
        newEventViewModel.eventLocation = loc
        newEventViewModel.eventLatLng = latlng

        geolocationList.removeAllViews()
        chooseLocationTextView.visibility = View.GONE

        newEventLocationEditText.setText(loc)
    }

    /*override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(NewEventViewModel::class.java)
        // TODO: Use the ViewModel
    }*/

}