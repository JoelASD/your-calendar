package com.mp.yourcalendar

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.navigation.fragment.navArgs
import kotlinx.android.synthetic.main.fragment_event_edit.*
import kotlinx.android.synthetic.main.new_event_fragment.*
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class EventEditFragment : Fragment(), DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    //private args: EventEditFragmentArgs by navArgs<Event
    private val args: EventEditFragmentArgs by navArgs<EventEditFragmentArgs>()

    private lateinit var currentEvent: Event
    private lateinit var editedEvent: Event

    var button: Int = 1

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
        // Initialize view with current events information
        setEventDetails()
    }

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

        // Notifications
        initNotificationViews(currentEvent.eventNotificationList)

        // Save button
        editEventSaveButton.setOnClickListener {
            //TODO: check if changes, save to db
        }

    }

    private fun initDateTimeOnClick() {
        // Start date
        editEventStartDateButton.text = currentEvent.eventStartDate
        editEventStartDateButton.setOnClickListener {
            val dParts: List<String> = editedEvent.eventStartDate.split("/")
            DatePickerDialog(requireContext(), this, dParts[2].toInt(), dParts[1].toInt(), dParts[0].toInt()).show() // year, month, day
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
            DatePickerDialog(requireContext(), this, dParts[2].toInt(), dParts[1].toInt(), dParts[0].toInt()).show()
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
                    // Set the chosen items id to eventType
                    //newEventViewModel.eventType = editEventTypeSpinner.adapter.getItemId(position).toInt()
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // Use the first item if nothing is selected by user
                    //newEventViewModel.eventType = 0
                }
            }
        }
    }

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
                    // Set the chosen items id to eventRepeat variable
                    //newEventViewModel.eventRepeat = newEventRepeatSpinner.adapter.getItemId(position).toInt()
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // Use first option if nothing selected
                    //newEventViewModel.eventRepeat = 0
                }
            }
        }
    }

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
            }
            // Set new row view to page
            editNotificationList.addView(notificationView)
        }
    }

    override fun onDateSet(p0: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        /*newEventViewModel.day = dayOfMonth
        newEventViewModel.month = month+1
        newEventViewModel.year = year

        runUpdate(button)*/
        updateDates(button, dayOfMonth, month, year)
    }

    override fun onTimeSet(p0: TimePicker?, hourOfDay: Int, minute: Int) {
        /*newEventViewModel.hour = hourOfDay
        newEventViewModel.minute = minute
        runUpdate(button)*/
        updateTimes(button, hourOfDay, minute)
    }

    private fun updateDateTimeViews() {
        editEventStartDateButton.text = editedEvent.eventStartDate
        editEventStartTimeButton.text = editedEvent.eventStartTime
        editEventEndDateButton.text = editedEvent.eventEndDate
        editEventEndTimeButton.text = editedEvent.eventEndTime
    }

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

                    // Now that dates are same, check that start time is not after endtime -> if yes, set endTime = startTime
                    val stp: List<String> = editedEvent.eventStartTime.split(":")
                    val etp: List<String> = editedEvent.eventEndTime.split(":")
                    if (LocalTime.of(stp[0].toInt(), stp[1].toInt()).isAfter(LocalTime.of(etp[0].toInt(), etp[1].toInt()))) {
                        editedEvent.eventEndTime = currentEvent.eventStartTime
                    }
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

                    // Now that dates are same, check that start time is not after endtime -> if yes, set endTime = startTime
                    val stp: List<String> = editedEvent.eventStartTime.split(":")
                    val etp: List<String> = editedEvent.eventEndTime.split(":")
                    if (LocalTime.of(stp[0].toInt(), stp[1].toInt()).isAfter(LocalTime.of(etp[0].toInt(), etp[1].toInt()))) {
                        editedEvent.eventEndTime = currentEvent.eventStartTime
                    }
                }
            }
        }
        updateDateTimeViews()
    }

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

    // Format new date string (that goes into view)
    private fun formatDate(day: Int, month: Int, year: Int): String{
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

        return if (day < 10 && month < 10) "0$day/0$month/$year".format(formatter)
        else if (day < 10 && month > 10) "0$day/$month/$year".format(formatter)
        else if (day > 10 && month < 10) "$day/0$month/$year".format(formatter)
        else "$day/$month/$year".format(formatter)
    }

    private fun formatTime(hour: Int, minute: Int): String{
        val formatter = DateTimeFormatter.ofPattern("HH:mm")

        return if (hour < 10 && minute < 10) "0$hour:0$minute".format(formatter)
        else if (hour < 10 && minute > 10) "0$hour:$minute".format(formatter)
        else if (hour > 10 && minute < 10) "$hour:0$minute".format(formatter)
        else "$hour:$minute".format(formatter)

    }

}