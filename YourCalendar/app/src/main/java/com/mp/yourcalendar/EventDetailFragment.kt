package com.mp.yourcalendar

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_event_detail.*
import org.json.JSONObject
import java.lang.Exception

class EventDetailFragment : Fragment(), OnMapReadyCallback {

    //val API_LINK: String = "https://api.openweathermap.org/data/2.5/weather?q="
    val API_LINK: String = "https://api.openweathermap.org/data/2.5/weather?lat="
    val API_ICON: String = "https://openweathermap.org/img/w/"
    val API_KEY: String = "5bdefed79ba336162a525526c85f2339"

    private val args: EventDetailFragmentArgs by navArgs<EventDetailFragmentArgs>()

    private lateinit var gMap: GoogleMap

    // Weather
    private lateinit var locTemperature: String
    private lateinit var locCondition: String
    private lateinit var locHumidity: String
    private lateinit var locUrl: String

    // Notification list
    private lateinit var notificationList: MutableList<EventNotification>


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_event_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        nameTextView.text = args.currentEvent.eventName
        startDateTimeTextView.text = "${args.currentEvent.eventStartDate} - ${args.currentEvent.eventStartTime}"
        endDateTimeTextView.text = "${args.currentEvent.eventEndDate} - ${args.currentEvent.eventEndTime}"

        nameTextView.setTextColor(ContextCompat.getColor(requireContext(),
            when(args.currentEvent.eventType){
                0 -> R.color.blue
                1 -> R.color.green
                2 -> R.color.red
                3 -> R.color.yellow
                4 -> R.color.pink
                5 -> R.color.purple
                6 -> R.color.brown
                else -> R.color.black
            }))

        repeatTextView.text =
                when(args.currentEvent.eventRepeat){
                    0 -> ""
                    1 -> "Once week"
                    2 -> "Once a month"
                    3 -> "Yearly"
                    else -> ""
                }

        // Notifications
        addNotificationViews()

        // Description
        if (args.currentEvent.eventDescription.isNullOrBlank()){
            desc.visibility = View.GONE
        } else {
            descriptionTextView.text = args.currentEvent.eventDescription
        }

        // Location
        if (args.currentEvent.eventLocLatLng.isNullOrBlank()){
            locView.visibility = View.GONE
        } else {
            if (mapView != null){
                mapView.onCreate(null)
                mapView.onResume()
                mapView.getMapAsync(this)

            }
            val locParts: List<String>? = args.currentEvent.eventLocLatLng?.split(" ")
            if (locParts != null) {
                loadWeatherForecast(locParts[0], locParts[1])
            }
        }

        // Edit button
        editEventButton.setOnClickListener {
            val action: NavDirections = EventDetailFragmentDirections.actionEventDetailToEventEdit(args.currentEvent)
            findNavController().navigate(action)
        }

        // Delete button
        deleteEventButton.setOnClickListener {
            deleteEvent(args.currentEvent.eventKey!!)

        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        MapsInitializer.initialize(this.context)

        val locParts: List<String>? = args.currentEvent.eventLocLatLng?.split(" ")

        if (locParts != null) {
            val locationLatLng = LatLng(locParts[0].toDouble(), locParts[1].toDouble())
            gMap = googleMap
            gMap.addMarker(MarkerOptions()
                .position(locationLatLng)
                .title(args.currentEvent.eventLocName))

            gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(locationLatLng, 15F))
        }
    }

    // Load forecast
    private fun loadWeatherForecast(lat: String, lng: String){
        //url for loading
        //val url = "$API_LINK$city&APPID=$API_KEY&units=metric&lang=fi"
        val url = "$API_LINK$lat&lon=$lng&appid=$API_KEY&units=metric"

        //JSON object request with volley
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null, Response.Listener<JSONObject> { response ->
                try {
                    //load ok, parse data from JSON
                    val mainJSONObject = response.getJSONObject("main")
                    val weatherArray = response.getJSONArray("weather")
                    val firstWeatherObject = weatherArray.getJSONObject(0)

                    // Temperature, condition, humidity
                    val temperature = mainJSONObject.getString("temp") + " ??C"
                    val condition = firstWeatherObject.getString("main")
                    val humidity = mainJSONObject.getString("humidity") + "%"

                    // Icon
                    val weatherIcon = firstWeatherObject.getString("icon")
                    val url = "$API_ICON$weatherIcon.png"

                    // Add items to variables
                    locTemperature = temperature
                    locCondition = condition
                    locHumidity = humidity
                    locUrl = url

                    setWeatherUI()
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.d("WEATHER", "***** error: $e")
                    Toast.makeText(this.context, "Error loading weather", Toast.LENGTH_LONG).show()
                }
            },
            Response.ErrorListener { error -> Log.d("WEATHER", "ERROR: $error") })
        // Start loading weather data with volley
        val queue = Volley.newRequestQueue(context)
        queue.add(jsonObjectRequest)
    }

    // Show weather
    private fun setWeatherUI(){
        locationTextView.text = args.currentEvent.eventLocName
        temperatureTextView.text = locTemperature
        conditionTextView.text = locCondition
        humidityTextView.text = locHumidity

        // Glide to get icon
        val requestOptions = RequestOptions()
        requestOptions.override(100,100)
        Glide.with(this)
            .load(locUrl)
            .apply(requestOptions)
            .into(conditionImageView)
    }

    // Add notification list info
    fun addNotificationViews(){
        if (args.currentEvent.eventNotificationList.size > 0) {
            for (item in args.currentEvent.eventNotificationList){
                val notificationView = layoutInflater.inflate(R.layout.row_notification_detail, null, false)
                val notifDateTimeText: TextView = notificationView.findViewById(R.id.notificationDateTimeTextView)
                notifDateTimeText.text = "${item.date} - ${item.time}"

                notifList.addView(notificationView)
            }
        } else {
            detailNotifications.visibility = View.GONE
        }

    }

    // Delete function
    private fun deleteEvent(key: String) {
        val UID = Firebase.auth.uid
        if (UID != null) {
            FirebaseDatabase.getInstance().getReference()
                    .child("users")
                    .child(UID).child(key)
                    .removeValue()
                    .addOnCompleteListener{
                        if(it.isComplete){
                            cancelNotifications(requireContext())
                            Toast.makeText(context, "Event was removed!", Toast.LENGTH_SHORT).show()
                            val action: NavDirections = EventDetailFragmentDirections.actionEventDetailToNavHome()
                            findNavController().navigate(action)
                        }
                    }
        } else {
            Log.d("DELETE_EVENT", "UID NOT FOUND")
        }
    }

    // Cancel all notifications
    private fun cancelNotifications(context: Context) {
        // Loop through current events notifications and cancel them
        for (notif in args.currentEvent.eventNotificationList) {
            val title = args.currentEvent.eventName
            val desc =
                when(notif.type) {
                    0 -> "$title has started!"
                    1 -> "$title will start in 5 minutes!"
                    2 -> "$title will start in 10 minutes!"
                    3 -> "$title will start in 15 minutes!"
                    4 -> "$title will start in 30 minutes!"
                    5 -> "$title will start in 1 hour!"
                    6 -> "$title will start in 2 hours!"
                    7 -> "$title will start tomorrow!"
                    8 -> "$title will start in a week!"
                    9 -> "$title has ended."
                    else -> ""
                }

            Log.d("ALARM", "Cancel NOTIF")
            val intent = Intent(context, NotificationReceiver::class.java)
            intent.putExtra("title", title)
            intent.putExtra("description", desc)
            val pending: PendingIntent = PendingIntent.getBroadcast(context, notif.rc!!, intent, PendingIntent.FLAG_UPDATE_CURRENT)

            // Cancel notification
            val manager: AlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            manager.cancel(pending)
        }
    }
}