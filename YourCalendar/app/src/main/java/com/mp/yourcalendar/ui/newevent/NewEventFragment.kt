package com.mp.yourcalendar.ui.newevent

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.get
import androidx.lifecycle.Observer
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.mp.yourcalendar.R
import kotlinx.android.synthetic.main.new_event_fragment.*
import java.text.DateFormat
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*

class NewEventFragment : Fragment(), DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
    // ViewModel
    private lateinit var newEventViewModel: NewEventViewModel

    // Name
    private lateinit var nameEditText: EditText
    // DateTime
    private lateinit var startDateButton: Button
    private lateinit var startTimeButton: Button
    private lateinit var endDateButton: Button
    private lateinit var endTimeButton: Button
    var button: Int = 0
    // Type
    private lateinit var typeSpinner: Spinner
    // Repeat
    private lateinit var repeatSpinner: Spinner
    // Description
    private lateinit var descriptionEditText: EditText
    // Location
    private lateinit var locationEditText: EditText
    // Notifications
    private lateinit var notificationLayoutList: LinearLayout
    private lateinit var addNotificationButton: Button
    // Create event button
    private lateinit var createEventButton: Button


    //Firebase
    private lateinit var database: DatabaseReference



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        newEventViewModel = ViewModelProvider(this).get(NewEventViewModel::class.java)
        val root = inflater.inflate(R.layout.new_event_fragment, container, false)

        newEventViewModel.initDateTimePicker()

        // Event name
        nameEditText = root.findViewById(R.id.newEventNameEditText)

        // Start date button listener
        startDateButton = root.findViewById(R.id.newEventStartDateButton)
        startDateButton.text = "${newEventViewModel.startDate}"
        startDateButton.setOnClickListener {
            DatePickerDialog(requireActivity(),this, newEventViewModel.startYear, newEventViewModel.startMonth-1, newEventViewModel.startDay).show()
            button = 1
        }

        // Start time button listener
        startTimeButton = root.findViewById(R.id.newEventStartTimeButton)
        startTimeButton.text = "${newEventViewModel.startTime}"
        startTimeButton.setOnClickListener {
            TimePickerDialog(requireActivity(), this, newEventViewModel.startHour, newEventViewModel.startMinute, true).show()
            button = 3
        }

        // End date button listener
        endDateButton = root.findViewById(R.id.newEventEndDateButton)
        endDateButton.text = "${newEventViewModel.endDate}"
        endDateButton.setOnClickListener {
            DatePickerDialog(requireActivity(), this, newEventViewModel.endYear, newEventViewModel.endMonth-1, newEventViewModel.endDay).show()
            button = 2
        }

        // End time button listener
        endTimeButton = root.findViewById(R.id.newEventEndTimeButton)
        endTimeButton.text = "${newEventViewModel.endTime}"
        endTimeButton.setOnClickListener {
            TimePickerDialog(requireActivity(), this, newEventViewModel.endHour, newEventViewModel.endMinute, true).show()
            button = 4
        }

        // Type spinner, array adapter
        typeSpinner = root.findViewById(R.id.newEventTypeSpinner)
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter.createFromResource(
                requireActivity(),
                R.array.type_array,
                android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            typeSpinner.adapter = adapter
            typeSpinner.onItemSelectedListener = object :
                    AdapterView.OnItemSelectedListener{
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    // Set the chosen items id to eventType
                    newEventViewModel.eventType = typeSpinner.adapter.getItemId(position).toInt()
                    //Log.d("******", "item selected:" + typeSpinner.adapter.getItemId(position))
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // Use the first item if nothing is selected by user
                    newEventViewModel.eventType = 0
                    //Log.d("XXXXXX", "first item: " + typeSpinner.adapter.getItem(0))
                }
            }
        }

        // Repeat spinner, array adapter
        repeatSpinner = root.findViewById(R.id.newEventRepeatSpinner)
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter.createFromResource(
                requireActivity(),
                R.array.repeat_array,
                android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            repeatSpinner.adapter = adapter
            repeatSpinner.onItemSelectedListener = object :
                    AdapterView.OnItemSelectedListener{
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    // Set the chosen items id to eventRepeat variable
                    newEventViewModel.eventRepeat = repeatSpinner.adapter.getItemId(position).toInt()
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // Use first option if nothing selected
                    newEventViewModel.eventRepeat = 0
                }
            }
        }

        // Description editText
        descriptionEditText = root.findViewById(R.id.newEventDescriptionEditText)

        // Location editText
        locationEditText = root.findViewById(R.id.newEventLocationEditText)

        // Notification "holder"
        notificationLayoutList = root.findViewById(R.id.notificationList)
        // Add notification button
        addNotificationButton = root.findViewById(R.id.newEventAddNotificationButton)
        addNotificationButton.setOnClickListener {
            addNotificationView()
        }

        // Create event button listener
        createEventButton = root.findViewById(R.id.newEventCreateButton)
        createEventButton.setOnClickListener {
            // Make sure also event name is given before saving new event to DB
            if (nameEditText.text.trim().isNotEmpty()){
                //validateAndReadNotifications()
                newEventViewModel.saveEvent(nameEditText.text.toString(), descriptionEditText.text.toString(), locationEditText.text.toString())
            } else {
                nameEditText.requestFocus()
                nameEditText.error = "You must set a name for the event!"
            }
        }

        return root
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
                startDateButton.text = newEventViewModel.startDate
                endDateButton.text = newEventViewModel.endDate
                startTimeButton.text = newEventViewModel.startTime
                endTimeButton.text = newEventViewModel.endTime
            }
            2 -> {
                newEventViewModel.updateDates(2, newEventViewModel.day, newEventViewModel.month, newEventViewModel.year)
                startDateButton.text = newEventViewModel.startDate
                endDateButton.text = newEventViewModel.endDate
                startTimeButton.text = newEventViewModel.startTime
                endTimeButton.text = newEventViewModel.endTime
            }
            3 -> {
                // Run updateTimes function
                newEventViewModel.updateTimes(3, newEventViewModel.hour, newEventViewModel.minute)
                // Set new times to view/buttons
                startTimeButton.text = newEventViewModel.startTime
                endTimeButton.text = newEventViewModel.endTime
                startDateButton.text = newEventViewModel.startDate
                endDateButton.text = newEventViewModel.endDate
            }
            4 -> {
                newEventViewModel.updateTimes(4, newEventViewModel.hour, newEventViewModel.minute)
                startTimeButton.text = newEventViewModel.startTime
                endTimeButton.text = newEventViewModel.endTime
                startDateButton.text = newEventViewModel.startDate
                endDateButton.text = newEventViewModel.endDate
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
        notificationLayoutList.addView(notificationView)
    }

    fun removeNotificationView(view: View){
        notificationLayoutList.removeView(view)
    }

    /*fun validateAndReadNotifications(){
        newEventViewModel.notificationList.clear()
        for (item in newEventViewModel.notificationList){

        }
    }*/

    /*ArrayAdapter.createFromResource(
    requireActivity(),
    R.array.repeat_array,
    android.R.layout.simple_spinner_item
    ).also { adapter ->
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        // Apply the adapter to the spinner
        repeatSpinner.adapter = adapter
        repeatSpinner.onItemSelectedListener = object :
                AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // Set the chosen items id to eventRepeat variable
                newEventViewModel.eventRepeat = repeatSpinner.adapter.getItemId(position).toInt()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Use first option if nothing selected
                newEventViewModel.eventRepeat = 0
            }
        }*/

    /*override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(NewEventViewModel::class.java)
        // TODO: Use the ViewModel
    }*/

}